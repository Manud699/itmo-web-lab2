
export class BaseValidator {
    constructor() {
        if (new.target === BaseValidator) {
            throw new Error("BaseValidator is an abstract class and cannot be instantiated directly.");
        }
    }

    validate(value) {
        throw new Error(`Method validate() must be implemented in ${this.constructor.name}`);
    }
}