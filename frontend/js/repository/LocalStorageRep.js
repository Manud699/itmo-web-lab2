export class LocalStorageRep {

    #storageKey; 

    constructor(storageKey = 'lab1_points') {
        this.#storageKey = storageKey; 
    }

    load() {
        try {
            const data = localStorage.getItem(this.#storageKey); 
            return data ? JSON.parse(data) : null;

        } catch (error) {
            return null; 
        }
    }

    save(obj) {
        localStorage.setItem(this.#storageKey, JSON.stringify(obj));
    }   

    clear(){
        localStorage.removeItem(this.#storageKey);
    }

}