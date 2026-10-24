package ru.itmo.web.lab2;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AppMain {

    private static final List<BigDecimal> X_VALUES = decimals("-2", "-1.5", "-1", "-0.5", "0", "0.5", "1", "1.5", "2");
    private static final List<BigDecimal> R_VALUES = decimals("1", "1.5", "2", "2.5", "3");
    private static final BigDecimal Y_MIN = new BigDecimal("-5");
    private static final BigDecimal Y_MAX = new BigDecimal("3");
    private static final int MAX_LENGTH = 15;

    private static final String CONTENT_LENGTH = "CONTENT_LENGTH";

    private record Point(BigDecimal x, BigDecimal y, BigDecimal r) {
    }

    public static void main(String[] args) {
        var fastCGIJava = new FCGIInterface();

        while (fastCGIJava.FCGIaccept() >= 0) {
            try {
                String body = readBody();
                Map<String, String> params = parseForm(body);
                Point point = validate(params);

                String answer = "<h1>Answer</h1>"
                        + "<p>x = " + point.x() + ", y = " + point.y() + ", r = " + point.r() + "</p>";
                sendResponse(200, "OK", answer);

            } catch (IllegalArgumentException e) {
                sendResponse(400, "Bad Request", "<h1>Error</h1><p>" + e.getMessage() + "</p>");

            } catch (Exception e) {
                e.printStackTrace(System.err);
                sendResponse(500, "Internal Server Error", "<h1>Error interno</h1>");
            }
        }
    }

    private static String readBody() throws IOException {
        String lengthText = FCGIInterface.request.params.getProperty(CONTENT_LENGTH, "0");
        int contentLength = Integer.parseInt(lengthText.trim());

        byte[] buffer = new byte[contentLength];
        int readBytes = 0;
        while (readBytes < contentLength) {
            int justRead = System.in.read(buffer, readBytes, contentLength - readBytes);
            if (justRead == -1) {
                break;
            }
            readBytes += justRead;
        }
        return new String(buffer, 0, readBytes, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> map = new HashMap<>();
        if (body.isEmpty()) return map;
        for (String item : body.split("&")) {
            if (item.isEmpty()) continue;
            String[] parts = item.split("=", 2);
            String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";
            if (map.containsKey(name)) {
                throw new IllegalArgumentException("Parámetro repetido: " + name);
            }
            map.put(name, value);
        }
        return map;
    }

    private static Point validate(Map<String, String> params) {
        BigDecimal x = parseNumber(params, "x");
        BigDecimal y = parseNumber(params, "y");
        BigDecimal r = parseNumber(params, "r");

        if (!contains(X_VALUES, x)) {
            throw new IllegalArgumentException("x debe ser uno de " + X_VALUES);
        }
        if (y.compareTo(Y_MIN) < 0 || y.compareTo(Y_MAX) > 0) {
            throw new IllegalArgumentException("y debe estar entre -5 y 3");
        }
        if (!contains(R_VALUES, r)) {
            throw new IllegalArgumentException("r debe ser uno de " + R_VALUES);
        }
        return new Point(x, y, r);
    }

    private static BigDecimal parseNumber(Map<String, String> params, String name) {
        String raw = params.get(name);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Falta el parámetro " + name);
        }
        String text = raw.trim().replace(',', '.');
        if (text.length() > MAX_LENGTH || !text.matches("-?\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException(name + " debe ser un número (máx. " + MAX_LENGTH + " caracteres)");
        }
        return new BigDecimal(text);
    }

    private static boolean contains(List<BigDecimal> allowed, BigDecimal value) {
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

    private static void sendResponse(int status, String statusText, String body) {
        try {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            String head = "HTTP/1.1 " + status + " " + statusText + "\r\n"
                    + "Content-Type: text/html; charset=utf-8\r\n"
                    + "Content-Length: " + bodyBytes.length + "\r\n"
                    + "\r\n";
            System.out.write(head.getBytes(StandardCharsets.UTF_8));
            System.out.write(bodyBytes);
            System.out.flush();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
