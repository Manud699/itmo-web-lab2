package ru.itmo.web.lab2.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PointValidator {

    private static final List<BigDecimal> X_VALUES = decimals("-2", "-1.5", "-1", "-0.5", "0", "0.5", "1", "1.5", "2");
    private static final List<BigDecimal> R_VALUES = decimals("1", "1.5", "2", "2.5", "3");
    private static final BigDecimal Y_MIN = new BigDecimal("-5");
    private static final BigDecimal Y_MAX = new BigDecimal("3");
    private static final int MAX_LENGTH = 15;

    public Point validate(Map<String, String> params) {
        BigDecimal x = parseNumber(params, "x");
        BigDecimal y = parseNumber(params, "y");
        BigDecimal r = parseNumber(params, "r");

        if (!contains(X_VALUES, x)) {
            throw new InvalidPointException("x must be one of " + X_VALUES);
        }
        if (y.compareTo(Y_MIN) < 0 || y.compareTo(Y_MAX) > 0) {
            throw new InvalidPointException("y must be between -5 and 3");
        }
        if (!contains(R_VALUES, r)) {
            throw new InvalidPointException("r must be one of " + R_VALUES);
        }
        return new Point(x, y, r);
    }

    private BigDecimal parseNumber(Map<String, String> params, String name) {
        String raw = params.get(name);
        if (raw == null || raw.isBlank()) {
            throw new InvalidPointException("Missing parameter: " + name);
        }
        String text = raw.trim().replace(',', '.');
        if (text.length() > MAX_LENGTH || !text.matches("-?\\d+(\\.\\d+)?")) {
            throw new InvalidPointException(name + " must be a number (max. " + MAX_LENGTH + " characters)");
        }
        return new BigDecimal(text);
    }

    private boolean contains(List<BigDecimal> allowed, BigDecimal value) {
        for (BigDecimal a : allowed) {
            if (a.compareTo(value) == 0) {
                return true;
            }
        }
        return false;
    }

    private static List<BigDecimal> decimals(String... values) {
        return Arrays.stream(values).map(BigDecimal::new).toList();
    }
}