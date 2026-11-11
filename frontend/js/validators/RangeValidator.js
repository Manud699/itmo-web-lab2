import { BaseValidator } from "./BaseValidator.js";
import { ResultValidation } from "./ResultValidation.js";

export class RangeValidator extends BaseValidator {
    #min;
    #max;

    constructor(min, max){
        super();
        this.#min = min;
        this.#max = max; 
    }

    validate(value) {
        if (value === '' || value === null || value === undefined) {
            return ResultValidation.fail('Missing required parameter: value cannot be empty or null.');
        }

        const normalizedValue = String(value).replace(",", "."); 
        const num = parseFloat(normalizedValue); 

        const formatRegex = /^-?\d+(\.\d{1,10})?$/;
        if (!formatRegex.test(normalizedValue)) {
            return ResultValidation.fail('Invalid format: value contains invalid characters or too many decimals (max 10).');
        }

        if (isNaN(num)) {
            return ResultValidation.fail('Invalid type: provided value must be a valid numeric format.');
        }

        if (num < this.#min || num > this.#max) {
            return ResultValidation.fail(`Value out of range: (${num} is strictly restricted to )[${this.#min}, ${this.#max}].`);
        }
        
        return ResultValidation.success(num); 
    }
}