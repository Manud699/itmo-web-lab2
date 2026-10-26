import { Observable } from "./Observable.js";
import { deepFreeze } from "./snapshot.js";


const RESTORE_TYPES = new Set(['RESTORE', 'UNDO', 'REDO']);
const isRestore = e => RESTORE_TYPES.has(e.type);

export class History extends Observable {
    #entries = [];
    #currentId = null;          
    #pendingIds = new Set();   
    #branches = [];            
    #nextId = 1;
    #nextBranchId = 1;


    record(type, oldValue, newValue, memento) {
        this.#discardPending();
        this.#push(type, oldValue, newValue, memento);
    }


    restore(id, type = 'RESTORE') {
        const target = this.#entries.find(e => e.id === id);
        if (!target) return null;

        this.#pendingIds = new Set(
            this.#entries.filter(e => e.id > target.base && !isRestore(e)).map(e => e.id)
        );
        this.#push(type, this.#currentId, id, target.memento, target.base);
        return target.memento;
        document
    }


    undo() {
        const head = this.current()?.base;
        const prev = this.#entries.filter(e => e.id < head && !isRestore(e)).at(-1);
        return prev ? this.restore(prev.id, 'UNDO') : null;
    }


    redo() {
        const next = this.#entries.find(e => this.#pendingIds.has(e.id));
        return next ? this.restore(next.id, 'REDO') : null;
    }


    restoreFromBranch(branchId, entryId) {
        const branch = this.#branches.find(b => b.id === branchId);
        const entry = branch?.entries.find(e => e.id === entryId);
        if (!entry) return null;

        this.record('RESTORE_BRANCH', this.#currentId, `B${branchId} #${entryId}`, entry.memento);
        return entry.memento;
    }


    clearHistory(memento) {
        const removed = this.#entries.length;
        this.#entries = [];
        this.#pendingIds.clear();
        this.#branches = [];
        this.#nextId = 1;
        this.#nextBranchId = 1;
        this.#push('CLEAR_HISTORY', `${removed} entries`, null, memento);
    }


    current() {
        return this.#entries.find(e => e.id === this.#currentId) ?? null;
    }


    getView() {
        const view = e => ({
            ...e,
            isCurrent: e.id === this.#currentId,
            isPending: this.#pendingIds.has(e.id)
        });
        return {
            entries: [...this.#entries].sort((a, b) => b.id - a.id).map(view),
            branches: [...this.#branches].reverse().map(b => ({
                ...b,
                entries: [...b.entries].sort((a, c) => c.id - a.id)
            }))
        };
    }


    toJSON() {
        return {
            entries: this.#entries,
            currentId: this.#currentId,
            pendingIds: [...this.#pendingIds],
            branches: this.#branches,
            nextId: this.#nextId,
            nextBranchId: this.#nextBranchId
        };
    }


    load(data) {
        if (!data || !Array.isArray(data.entries)) return;
        const byId = new Map(data.entries.map(e => [e.id, e]));
        const baseOf = e => e.base ?? (isRestore(e) ? (byId.get(e.newValue)?.base ?? e.newValue) : e.id);
        const freeze = e => ({ ...e, memento: deepFreeze(e.memento) });

        this.#entries = data.entries.map(e => freeze({ ...e, base: baseOf(e) }));
        this.#currentId = data.currentId;
        this.#pendingIds = new Set(data.pendingIds ?? []);
        this.#branches = (data.branches ?? []).map(b => ({ ...b, entries: b.entries.map(freeze) }));
        this.#nextId = data.nextId;
        this.#nextBranchId = data.nextBranchId ?? this.#branches.length + 1;
        this.notify(this.getView());
    }


    #discardPending() {
        if (this.#pendingIds.size === 0) return;
        const discarded = this.#entries.filter(e => this.#pendingIds.has(e.id));
        this.#branches.push({
            id: this.#nextBranchId++,
            fromId: this.current()?.base ?? null,   
            time: Date.now(),
            entries: discarded
        });
        this.#entries = this.#entries.filter(e => !this.#pendingIds.has(e.id));
        this.#pendingIds.clear();
    }


    #push(type, oldValue, newValue, memento, base) {
        const id = this.#nextId++;
        this.#entries.push({ id, base: base ?? id, type, oldValue, newValue, time: Date.now(), memento });
        this.#currentId = id;
        this.notify(this.getView());
    }
}
