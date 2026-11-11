package ru.itmo.web.lab2.session;

import ru.itmo.web.lab2.http.SetCookie;

import java.time.Duration;

/** The only place that decides the name and the attributes of the session cookie. */
public class SessionCookies {

    public static final String NAME = "SESSIONID";

    private final Duration timeout;

    public SessionCookies(Duration timeout) {
        this.timeout = timeout;
    }

    public SetCookie create(String sessionId) {
        return new SetCookie(NAME, sessionId, timeout.toSeconds(), "/", true, false, "Strict");
    }

    public SetCookie expire() {
        return new SetCookie(NAME, "", 0, "/", true, false, "Strict");
    }
}
