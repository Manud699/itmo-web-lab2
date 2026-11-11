export class ResultValidation {
    #isValid; 
    #payload; 

    constructor(isValid, payload = null) {
        this.#isValid = Boolean(isValid);
        this.#payload = payload; 
    }

    get isValid() {
        return this.#isValid; 
    }

    get data() {
        return this.#isValid ? this.#payload : null; 
    }

    get errors() {
        return !this.#isValid ? this.#payload : {}; 
    }

    static success(dataObject) {
        return new ResultValidation(true, dataObject);
    }

    static fail(errorObject) {
        return new ResultValidation(false, errorObject);
    }
}