import { ResultValidation } from "./ResultValidation.js";

export class ValidateForm {
    #xValidator;
    #yValidator;
    #rValidator;

    constructor(xValidator, yValidator, rValidator) {
        this.#xValidator = xValidator;
        this.#yValidator = yValidator;
        this.#rValidator = rValidator;
    }

    validateForm(xRaw, yRaw, rRaw) {
        const errors = {};

        const resultX = this.#xValidator.validate(xRaw);
        const resultY = this.#yValidator.validate(yRaw);
        const resultR = this.#rValidator.validate(rRaw);

        if (!resultX.isValid) errors.x = resultX.errors; 
        if (!resultY.isValid) errors.y = resultY.errors;
        if (!resultR.isValid) errors.r = resultR.errors;
        
        if (Object.keys(errors).length > 0) {
            return ResultValidation.fail(errors);
        }

        return ResultValidation.success({
            x: resultX.data,
            y: resultY.data,
            r: resultR.data
        });
    }
}