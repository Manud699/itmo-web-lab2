package ru.itmo.web.lab2.command;

import ru.itmo.web.lab2.http.Command;
import ru.itmo.web.lab2.http.HttpException;
import ru.itmo.web.lab2.http.Request;
import ru.itmo.web.lab2.http.Response;
import ru.itmo.web.lab2.session.SessionManager;

/** Decorator: runs the wrapped command only for ADMIN sessions, otherwise answers 403. */
public class AdminOnlyCommand implements Command {

    private final SessionManager sessionManager;
    private final Command inner;

    public AdminOnlyCommand(SessionManager sessionManager, Command inner) {
        this.sessionManager = sessionManager;
        this.inner = inner;
    }

    @Override
    public Response execute(Request request) {
        if (!sessionManager.isAdmin(request.sessionId())) {
            throw new HttpException(403, "Admin role required");
        }
        return inner.execute(request);
    }
}