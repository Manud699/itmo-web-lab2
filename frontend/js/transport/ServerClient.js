import { ServerError } from './ServerError.js';

export class ServerClient {

    #baseUrl;
    #timeoutMs;

    constructor(baseUrl, timeoutMs = 5000) {
        this.#baseUrl = baseUrl;
        this.#timeoutMs = timeoutMs;
    }

    async check(x, y, r) {
        const body = new URLSearchParams({ x: String(x), y: String(y), r: String(r) });
        return this.#request('POST', '/check', body);
    }

    async results() {
        return this.#request('GET', '/results');
    }

    async clear() {
        return this.#request('POST', '/clear');
    }

    async #request(method, path, body) {
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), this.#timeoutMs);

        let response;
        try {
            response = await fetch(this.#baseUrl + path, {
                method,
                body,
                headers: { 'Accept': 'application/json' },
                signal: controller.signal
            });
        } catch (e) {
            throw new ServerError(0, e.name === 'AbortError'
                ? 'The server did not respond in time'
                : 'No connection to the server');
        } finally {
            clearTimeout(timer);
        }

        const data = await this.#readJson(response);
        if (!response.ok) {
            throw new ServerError(response.status, data?.error ?? `HTTP error ${response.status}`);
        }
        if (data === null) {
            throw new ServerError(response.status, 'The server response is not valid JSON');
        }
        return data;
    }

    async #readJson(response) {
        try {
            return await response.json();
        } catch {
            return null;
        }
    }
}
