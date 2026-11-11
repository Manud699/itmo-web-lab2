package ru.itmo.web.lab2.session;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Keeps every active session and turns the id from the cookie into a Session.
 * An unknown or expired id gets a new session with a new random id.
 */
public class SessionManager {

    private final Map<String, Session> sessions = new HashMap<>();
    private final Duration timeout;
    private final Clock clock;
    private final Consumer<String> onExpired;

    public SessionManager(Duration timeout, Clock clock, Consumer<String> onExpired) {
        this.timeout = timeout;
        this.clock = clock;
        this.onExpired = onExpired;
    }

    public Session resolve(String sessionId) {
        Instant now = clock.instant();
        removeExpired(now);
        Session session = sessionId == null ? null : sessions.get(sessionId);
        if (session == null) {
            session = new Session(UUID.randomUUID().toString(), now);
            sessions.put(session.id(), session);
        } else {
            session.touch(now);
        }
        return session;
    }

    public Duration timeout() {
        return timeout;
    }

    public boolean isAdmin(String sessionId) {
        Session session = sessions.get(sessionId);
        return session != null && session.role() == Role.ADMIN;
    }

    /**
     * Login: the old id is thrown away and an ADMIN session with a new id is created.
     * Changing the id prevents session fixation (an attacker cannot reuse an id he already knew).
     */
    public Session promoteToAdmin(String oldSessionId) {
        sessions.remove(oldSessionId);
        Session session = new Session(UUID.randomUUID().toString(), clock.instant());
        session.setRole(Role.ADMIN);
        sessions.put(session.id(), session);
        return session;
    }

    public void invalidate(String sessionId) {
        if (sessions.remove(sessionId) != null) {
            onExpired.accept(sessionId);
        }
    }

    private void removeExpired(Instant now) {
        Iterator<Session> it = sessions.values().iterator();
        while (it.hasNext()) {
            Session session = it.next();
            if (session.isExpired(now, timeout)) {
                it.remove();
                onExpired.accept(session.id());
            }
        }
    }
}
