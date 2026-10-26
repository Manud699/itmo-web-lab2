import { Observable } from "./Observable.js";
import { takeSnapshot } from "./snapshot.js";


export function createInitialsState() {
    return {
        form: {x:null, y:'', r:null }, 
        points: []
    }
}


export class AppStore extends Observable {
    #state;

    constructor(initialState = createInitialsState()){
        super(); 
        this.#state = structuredClone(initialState);
    }

    getState(){
        return structuredClone(this.#state);
    }


    setX(x) { return this.#set('x', x); }
    setY(y) { return this.#set('y', y); }
    setR(r) { return this.#set('r', r); }

    setPoints(points){
        this.#state.points = structuredClone(points);
        this.notify(this.getState());
    }
    
    createMemento(){
        return takeSnapshot(this.#state);
    }

    restoreMemento(memento){
        const points = this.#state.points;
        this.#state = structuredClone(memento);
        this.#state.points = points;
        this.notify(this.getState());
    }

    #set(field, value){
        const old = this.#state.form[field]; 
        this.#state.form[field] = value; 
        this.notify(this.getState());
        return old;
    }

} 