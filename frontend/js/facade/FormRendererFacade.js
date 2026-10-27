import { formatTime } from '../view/AppRenderer.js';

export class FormRendererFacade {
    #store;
    #history;
    #formValidator;   
    #fieldValidators; 
    #invalidFields = new Set(); 
    #server;
    #sending = false;

    constructor(store, history, formValidator, fieldValidators, server) {
        this.#store = store;
        this.#history = history;
        this.#formValidator = formValidator;
        this.#fieldValidators = fieldValidators;
        this.#server = server;
    }

    setupEventListeners() {
        document.getElementById('selectX')
            .addEventListener('change', e => this.#handleFieldChange('x', e.target.value));
        document.getElementById('inputY')
            .addEventListener('change', e => this.#handleFieldChange('y', e.target.value));
        document.querySelectorAll('input[name=inputR]').forEach(el => 
            el.addEventListener('change', e => this.#handleFieldChange('r', e.target.checked ? e.target.value : '')));

        document.getElementById('formulario')
            .addEventListener('submit', e => this.#handleFormSubmit(e));
        document.getElementById('clear-btn')
            ?.addEventListener('click', () => this.#handleClear());
        document.getElementById('clear-history-btn')
            ?.addEventListener('click', () => this.#handleClearHistory());

        document.addEventListener('keydown', e => this.#handleShortcut(e));
    }

    async loadResults() {
        try {
            const data = await this.#server.results();
            this.#store.setPoints(data.history);
            this.#showServerTime(data.currentTime);
            this.#showStatus('');
        } catch (e) {
            this.#showStatus(e.message);
        }
    }

    restore(id) {
        this.#applyMemento(this.#history.restore(id));
    }

    restoreFromBranch(branchId, entryId) {
        this.#applyMemento(this.#history.restoreFromBranch(branchId, entryId));
    }

    #applyMemento(memento) {
        if (!memento) return;
        this.#invalidFields.clear();
        ['x', 'y', 'r'].forEach(f => this.#clearError(f));
        this.#store.restoreMemento(memento);
    }

    #handleShortcut(e) {
        if (!(e.ctrlKey || e.metaKey)) return;
        const isUndo = e.code === 'KeyZ' && !e.shiftKey;
        const isRedo = (e.code === 'KeyZ' && e.shiftKey) || e.code === 'KeyY';
        if (!isUndo && !isRedo) return;
        if (e.target instanceof HTMLInputElement && e.target.type === 'text') return;

        e.preventDefault();
        this.#applyMemento(isUndo ? this.#history.undo() : this.#history.redo());
    }

    #handleFieldChange(field, raw) {
        this.#clearError(field);
        const result = this.#fieldValidators[field].validate(raw);
        if (!result.isValid) {
            this.#showError(field, result.errors);
            this.#invalidFields.add(field);
            return;                               
        }
        this.#invalidFields.delete(field);
        
        const value = field === 'y' ? String(raw).trim() : result.data;
        const current = this.#store.getState().form[field];
        if (current === value) return;            

        const setter = { x: 'setX', y: 'setY', r: 'setR' }[field];
        const old = this.#store[setter](value);
        this.#history.record(`CHANGE_${field.toUpperCase()}`, old, value,
                            this.#store.createMemento());
    }

    async #handleFormSubmit(event) {
        event.preventDefault();

        if (this.#sending || this.#invalidFields.size > 0) return;
        ['x', 'y', 'r'].forEach(f => this.#clearError(f));

        const { form } = this.#store.getState();
        const validation = this.#formValidator.validateForm(form.x, form.y, form.r);
        if (!validation.isValid) {
            Object.entries(validation.errors).forEach(([f, msg]) => this.#showError(f, msg));
            return;
        }

        const { x, y, r } = validation.data;
        this.#setSending(true);
        try {
            const data = await this.#server.check(x, y, r);
            this.#store.setPoints(data.history);
            this.#history.record('ADD_POINT', null, data.result, this.#store.createMemento());
            this.#showServerTime(data.currentTime);
            this.#showStatus('');
        } catch (e) {
            this.#showStatus(e.message);
        } finally {
            this.#setSending(false);
        }
    }

    #setSending(sending) {
        this.#sending = sending;
        document.getElementById('submit-btn').disabled = sending;
    }

    #showStatus(message) {
        document.getElementById('server-status').textContent = message;
    }

    #showServerTime(ms) {
        document.getElementById('server-time').textContent = `Server time: ${formatTime(ms)}`;
    }

    async #handleClear() {
        if (this.#sending) return;
        const old = this.#store.getState().points.length;
        this.#setSending(true);
        try {
            const data = await this.#server.clear();
            this.#store.setPoints(data.history);
            this.#history.record('CLEAR_POINTS', old, 0, this.#store.createMemento());
            this.#showServerTime(data.currentTime);
            this.#showStatus('');
        } catch (e) {
            this.#showStatus(e.message);
        } finally {
            this.#setSending(false);
        }
    }

    #handleClearHistory() {
        if (!confirm('Clear the whole action log? The current state is kept.')) return;
        this.#history.clearHistory(this.#store.createMemento());
    }

    #clearError(field) {
        document.getElementById(`input-check-${field}`)?.classList.add('hidden');
    }

    #showError(field, message) {
        const errorContainer = document.getElementById(`input-check-${field}`); 
        const textSpan = errorContainer.querySelector('.msg-content');
        
        if (textSpan) {
            textSpan.textContent = message; 
        }
        
        if (errorContainer) {
            errorContainer.classList.remove('hidden');
        }
    }
}