import { calculateHit } from '../core/geometry.js';

const timeFormatter = new Intl.DateTimeFormat('ru-RU',{
    year:'numeric', month:'numeric', day:'numeric',
    hour:'2-digit', minute:'2-digit', second:'2-digit'
}); 

export const formatTime = ms => timeFormatter.format(new Date(ms));

export class AppRenderer{
    #plane; 

    constructor(coordinatePlaneRenderer){
        this.#plane = coordinatePlaneRenderer; 
    } 

    render(state){
        this.#renderForm(state.form);
        this.#renderResults(state.points);
        this.#renderCanvas(state.form.r, state.points);
    } 

    #renderForm({x, y, r}){
        document.querySelectorAll('input[name=inputX]')
            .forEach(el => { el.checked = Number(el.value) === x; });
        document.getElementById('inputY').value = y; 
        document.getElementById('selectR').value = r ?? ''; 
    }

    #renderResults(points){
        const tbody = document.querySelector('#table-results tbody');
        tbody.replaceChildren(...points.map(p => this.#row([
            p.x, p.y, p.r, formatTime(p.time), p.isHit ? 'Hit' : 'Miss'
        ])));
    }

    #renderCanvas(r,points){
        this.#plane.drawBaseGraph(r);
        points.forEach(p => {
            const isHit = r ? calculateHit(p.x, p.y, r) : p.isHit;
            this.#plane.drawPoint(p.x, p.y, isHit);
        });
    }

    renderLog({ entries, branches }, { onRestore, onRestoreBranch }) {
        const tbody = document.querySelector('#table-log tbody');
        tbody.replaceChildren(...entries.map(e => {
            const row = this.#logRow(e, () => onRestore(e.id));
            row.classList.toggle('current', e.isCurrent);
            row.classList.toggle('pending', e.isPending);
            row.querySelector('button').disabled = e.isCurrent;
            return row;
        }));
        this.#renderBranches(branches, onRestoreBranch);
    }

    #renderBranches(branches, onRestoreBranch) {
        document.getElementById('branches').classList.toggle('hidden', branches.length === 0);
        document.getElementById('branches-count').textContent = branches.length;

        document.getElementById('branches-list').replaceChildren(...branches.map(b => {
            const title = document.createElement('p');
            title.className = 'branch-title';
            title.textContent = `Branch B${b.id} · split from #${b.fromId ?? '—'} · ${formatTime(b.time)}`;

            const table = document.createElement('table');
            table.className = 'data-table log-table';
            const tbody = document.createElement('tbody');
            tbody.append(...b.entries.map(e => this.#logRow(e, () => onRestoreBranch(b.id, e.id))));
            table.append(tbody);

            const wrap = document.createElement('div');
            wrap.className = 'branch';
            wrap.append(title, table);
            return wrap;
        }));
    }

    #logRow(e, onClick) {
        const row = this.#row([
            e.id, e.type, this.#fmt(e.oldValue), this.#fmt(e.newValue), formatTime(e.time)
        ]);
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = 'Restore';
        btn.addEventListener('click', onClick);
        const td = document.createElement('td');
        td.append(btn);
        row.append(td);
        return row;
    }

    #row(cells) {
        const tr = document.createElement('tr');
        cells.forEach(text => {
            const td = document.createElement('td');
            td.textContent = text; 
            tr.append(td)
        });
        return tr
    }

    #fmt(v) {
        if (v === null || v === undefined || v === '') return '—';
        return typeof v === 'object' ? `(${v.x}; ${v.y}; ${v.r})` : String(v);
    }

} 