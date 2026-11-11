package ru.itmo.web.lab2.domain;

import java.time.Instant;

/** One row of the history: the point, whether it hit, when it was checked and how long it took. */
public record HitResult(Point point, boolean hit, Instant time, long execTimeNs) {
}