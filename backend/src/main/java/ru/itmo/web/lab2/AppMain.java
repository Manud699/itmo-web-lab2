package ru.itmo.web.lab2;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.http.Command;
import ru.itmo.web.lab2.http.HttpException;
import ru.itmo.web.lab2.http.Request;
import ru.itmo.web.lab2.http.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
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
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);


    private static final String CONTENT_LENGTH = "CONTENT_LENGTH";
    private static final String HTML = "text/html; charset=utf-8";

    private record Point(BigDecimal x, BigDecimal y, BigDecimal r) {
    }

    private record HitResult(Point point, boolean hit, Instant time, long execTimeNs) {
    }

    public static void main(String[] args) {
        var fastCGIJava = new FCGIInterface();

        List<HitResult> history = new ArrayList<>();
        Command checkCommand = request -> check(request, history);

        while (fastCGIJava.FCGIaccept() >= 0) {
            try {
                Request request = readRequest();
                Response response = checkCommand.execute(request);
                sendResponse(response);

            } catch (HttpException e) {
                sendResponse(new Response(e.getStatus(), HTML, "<h1>Error</h1><p>" + e.getMessage() + "</p>"));

            } catch (Exception e) {
                e.printStackTrace(System.err);
                sendResponse(new Response(500, HTML, "<h1>Internal server error</h1>"));
            }
        }
    }

    private static Response check(Request request, List<HitResult> history) {
        long start = System.nanoTime();

        Point point = validate(request.params());
        boolean hit = isHit(point);

        long execTimeNs = System.nanoTime() - start;
        Instant now = Instant.now();

        HitResult result = new HitResult(point, hit, now, execTimeNs);
        history.add(result);

        return new Response(200, HTML, renderPage(result, history, now));
    }

    private static Request readRequest() throws IOException {
        String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD", "GET");
        String path = FCGIInterface.request.params.getProperty("PATH_INFO", "");
        return new Request(method, path, parseForm(readBody()));
    }

    private static String readBody() throws IOException {
        String lengthText = FCGIInterface.request.params.getProperty(CONTENT_LENGTH, "0");
        int contentLength;
        try {
            contentLength = Integer.parseInt(lengthText.trim());
        } catch (NumberFormatException e) {
            throw new HttpException(400, "Invalid Content-Length");
        }

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
                throw new HttpException(400, "Duplicate parameter: " + name);
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
            throw new HttpException(400, "x must be one of " + X_VALUES);
        }
        if (y.compareTo(Y_MIN) < 0 || y.compareTo(Y_MAX) > 0) {
            throw new HttpException(400, "y must be between -5 and 3");
        }
        if (!contains(R_VALUES, r)) {
            throw new HttpException(400, "r must be one of " + R_VALUES);
        }
        return new Point(x, y, r);
    }

    private static boolean isHit(Point p) {
        return inCircle(p.x(), p.y(), p.r()) || inRectangle(p.x(), p.y(), p.r()) || inTriangle(p.x(), p.y(), p.r());
    }


    private static boolean inCircle(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (x.signum() < 0 || y.signum() < 0) return false;
        BigDecimal d = x.multiply(x).add(y.multiply(y)).multiply(FOUR);
        return d.compareTo(r.multiply(r)) <= 0;
    }


    private static boolean inRectangle(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (x.signum() > 0 || y.signum() < 0) return false;
        return x.compareTo(r.negate()) >= 0 && y.compareTo(r) <= 0;
    }


    private static boolean inTriangle(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (x.signum() > 0 || y.signum() > 0) return false;
        BigDecimal twoX = TWO.multiply(x);
        return twoX.compareTo(r.negate()) >= 0 && twoX.add(y).add(r).signum() >= 0;
    }

    private static BigDecimal parseNumber(Map<String, String> params, String name) {
        String raw = params.get(name);
        if (raw == null || raw.isBlank()) {
            throw new HttpException(400, "Missing parameter: " + name);
        }
        String text = raw.trim().replace(',', '.');
        if (text.length() > MAX_LENGTH || !text.matches("-?\\d+(\\.\\d+)?")) {
            throw new HttpException(400, name + " must be a number (max. " + MAX_LENGTH + " characters)");
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

    private static String renderPage(HitResult result, List<HitResult> history, Instant now) {
        StringBuilder html = new StringBuilder();
        html.append("<h1>Result</h1>")
                .append("<p>").append(result.hit() ? "Inside the area" : "Outside the area").append("</p>")
                .append("<p>Current time: ").append(now).append("</p>")
                .append("<p>Execution time: ").append(result.execTimeNs() / 1000).append(" µs</p>")
                .append("<table border=\"1\"><tr><th>x</th><th>y</th><th>r</th><th>Result</th><th>Time</th><th>µs</th></tr>");
        for (HitResult row : history) {
            html.append("<tr>")
                    .append("<td>").append(row.point().x()).append("</td>")
                    .append("<td>").append(row.point().y()).append("</td>")
                    .append("<td>").append(row.point().r()).append("</td>")
                    .append("<td>").append(row.hit() ? "Yes" : "No").append("</td>")
                    .append("<td>").append(row.time()).append("</td>")
                    .append("<td>").append(row.execTimeNs() / 1000).append("</td>")
                    .append("</tr>");
        }
        return html.append("</table>").toString();
    }

    private static void sendResponse(Response response) {
        try {
            byte[] bodyBytes = response.body().getBytes(StandardCharsets.UTF_8);
            String head = "HTTP/1.1 " + response.status() + " " + reasonPhrase(response.status()) + "\r\n"
                    + "Content-Type: " + response.contentType() + "\r\n"
                    + "Content-Length: " + bodyBytes.length + "\r\n"
                    + "\r\n";
            System.out.write(head.getBytes(StandardCharsets.UTF_8));
            System.out.write(bodyBytes);
            System.out.flush();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static String reasonPhrase(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            default -> "Internal Server Error";
        };
    }
}
