package ru.itmo.web.lab2.command;

import ru.itmo.web.lab2.http.Command;
import ru.itmo.web.lab2.http.Json;
import ru.itmo.web.lab2.http.Request;
import ru.itmo.web.lab2.http.Response;
import ru.itmo.web.lab2.session.SessionCookies;
import ru.itmo.web.lab2.session.SessionManager;

/** POST /logout: ends the session on the server and tells the browser to delete the cookie. */
public class LogoutCommand implements Command {

    private final SessionManager sessionManager;
    private final SessionCookies sessionCookies;

    public LogoutCommand(SessionManager sessionManager, SessionCookies sessionCookies) {
        this.sessionManager = sessionManager;
        this.sessionCookies = sessionCookies;
    }

    @Override
    public Response execute(Request request) {
        sessionManager.invalidate(request.sessionId());
        return new Response(200, Json.CONTENT_TYPE, "{\"loggedOut\":true}")
                .withCookie(sessionCookies.expire());
    }
}
