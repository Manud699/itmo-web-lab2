package ru.itmo.web.lab2.domain;

import java.math.BigDecimal;

public class AreaChecker {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);

    public boolean isHit(Point p) {
        return inCircle(p.x(), p.y(), p.r()) || inRectangle(p.x(), p.y(), p.r()) || inTriangle(p.x(), p.y(), p.r());
    }

    private boolean inCircle(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (x.signum() < 0 || y.signum() < 0) return false;
        BigDecimal d = x.multiply(x).add(y.multiply(y)).multiply(FOUR);
        return d.compareTo(r.multiply(r)) <= 0;
    }

    private boolean inRectangle(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (x.signum() > 0 || y.signum() < 0) return false;
        return x.compareTo(r.negate()) >= 0 && y.compareTo(r) <= 0;
    }

    private boolean inTriangle(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (x.signum() > 0 || y.signum() > 0) return false;
        BigDecimal twoX = TWO.multiply(x);
        return twoX.compareTo(r.negate()) >= 0 && twoX.add(y).add(r).signum() >= 0;
    }
}