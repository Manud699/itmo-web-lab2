package ru.itmo.web.lab2.session;

import java.time.Duration;
import java.time.Instant;

/**
 * Server-side data of one client. The cookie only carries the id;
 */
public class Session {

    private final String id;
    private final Instant createdAt;
    private Instant lastAccess;
    private Role role = Role.USER;

    public Session(String id, Instant now) {
        this.id = id;
        this.createdAt = now;
        this.lastAccess = now;
    }

    public String id() {
        return id;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Role role() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void touch(Instant now) {
        lastAccess = now;
    }

    public boolean isExpired(Instant now, Duration timeout) {
        return lastAccess.plus(timeout).isBefore(now);
    }
}
