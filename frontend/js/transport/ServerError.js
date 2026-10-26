export class ServerError extends Error {

    constructor(status, message) {
        super(message);
        this.name = 'ServerError';
        this.status = status;
    }
}
