

export class Observable {

    #listeners = new Set(); 

    subscribe(listener){
        this.#listeners.add(listener);
        return () => this.#listeners.delete(listener);
    }

    notify(payload){
        this.#listeners.forEach(listener => listener(payload));
    }

} 