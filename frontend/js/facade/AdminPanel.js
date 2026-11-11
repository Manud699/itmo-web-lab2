/**
 * Small session panel: admin login, highest point of all users and logout.
 * Every server message is shown with textContent, never innerHTML (no XSS).
 */
export class AdminPanel {
    #server;
    #onSessionChanged;

    constructor(server, onSessionChanged) {
        this.#server = server;
        this.#onSessionChanged = onSessionChanged;
    }

    setupEventListeners() {
        document.getElementById('admin-form')
            .addEventListener('submit', e => this.#handleLogin(e));
        document.getElementById('highest-btn')
            .addEventListener('click', () => this.#handleHighest());
        document.getElementById('logout-btn')
            .addEventListener('click', () => this.#handleLogout());
    }

    async #handleLogin(event) {
        event.preventDefault();
        const input = document.getElementById('admin-password');
        try {
            const data = await this.#server.adminLogin(input.value);
            this.#showRole(data.role);
            this.#showOutput('Logged in as admin');
        } catch (e) {
            this.#showOutput(e.message);
        } finally {
            input.value = '';
        }
    }

    async #handleHighest() {
        try {
            const { highest, total } = await this.#server.highest();
            this.#showOutput(highest
                ? `Highest of ${total} points: x=${highest.x}, y=${highest.y}, r=${highest.r} (${highest.hit ? 'hit' : 'miss'})`
                : 'No points yet');
        } catch (e) {
            this.#showOutput(e.message);
        }
    }

    async #handleLogout() {
        try {
            await this.#server.logout();
            this.#showRole('USER');
            this.#showOutput('Session closed');
            this.#onSessionChanged();
        } catch (e) {
            this.#showOutput(e.message);
        }
    }

    #showRole(role) {
        document.getElementById('admin-role').textContent = `Role: ${role}`;
    }

    #showOutput(message) {
        document.getElementById('admin-output').textContent = message;
    }
}
