package ru.itmo.web.lab2.command;

import ru.itmo.web.lab2.http.Command;
import ru.itmo.web.lab2.http.HttpException;
import ru.itmo.web.lab2.http.Json;
import ru.itmo.web.lab2.http.Request;
import ru.itmo.web.lab2.http.Response;
import ru.itmo.web.lab2.repository.ResultRepository;
import ru.itmo.web.lab2.session.Session;
import ru.itmo.web.lab2.session.SessionCookies;
import ru.itmo.web.lab2.session.SessionManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** POST /admin/login (password=...): gives the session the ADMIN role under a new id. */
public class AdminLoginCommand implements Command {

    private final SessionManager sessionManager;
    private final SessionCookies sessionCookies;
    private final ResultRepository repository;
    private final String adminPassword;

    /** adminPassword comes from -Dadmin.password; null means admin login is disabled. */
    public AdminLoginCommand(SessionManager sessionManager, SessionCookies sessionCookies,
                             ResultRepository repository, String adminPassword) {
        this.sessionManager = sessionManager;
        this.sessionCookies = sessionCookies;
        this.repository = repository;
        this.adminPassword = adminPassword;
    }

    @Override
    public Response execute(Request request) {
        String password = request.param("password");
        if (password == null || password.isEmpty()) {
            throw new HttpException(400, "Parameter 'password' is required");
        }
        if (adminPassword == null || !samePassword(password, adminPassword)) {
            throw new HttpException(401, "Wrong password");
        }
        Session admin = sessionManager.promoteToAdmin(request.sessionId());
        repository.moveHistory(request.sessionId(), admin.id());
        return new Response(200, Json.CONTENT_TYPE, "{\"role\":\"ADMIN\"}")
                .withCookie(sessionCookies.create(admin.id()));
    }

    /** Compares in constant time, so the response time does not reveal how many characters matched. */
    private boolean samePassword(String given, String expected) {
        return MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
