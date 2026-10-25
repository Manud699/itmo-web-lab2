package ru.itmo.web.lab2;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.domain.AreaChecker;
import ru.itmo.web.lab2.domain.HitResult;
import ru.itmo.web.lab2.domain.InvalidPointException;
import ru.itmo.web.lab2.domain.Point;
import ru.itmo.web.lab2.domain.PointValidator;
import ru.itmo.web.lab2.http.Command;
import ru.itmo.web.lab2.http.HttpException;
import ru.itmo.web.lab2.http.Request;
import ru.itmo.web.lab2.http.Response;
import ru.itmo.web.lab2.repository.InMemoryResultRepository;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppMain {

    private static final String CONTENT_LENGTH = "CONTENT_LENGTH";
    private static final String HTML = "text/html; charset=utf-8";

    public static void main(String[] args) {
        var fastCGIJava = new FCGIInterface();

        PointValidator validator = new PointValidator();
        AreaChecker areaChecker = new AreaChecker();
        var repository = new InMemoryResultRepository();

        Command checkCommand = request -> check(request, validator, areaChecker, repository);

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

    private static Response check(Request request, PointValidator validator, AreaChecker areaChecker,
                                  InMemoryResultRepository repository) {
        long start = System.nanoTime();

        Point point;
        try {
            point = validator.validate(request.params());
        } catch (InvalidPointException e) {
            throw new HttpException(400, e.getMessage());
        }
        boolean hit = areaChecker.isHit(point);

        long execTimeNs = System.nanoTime() - start;
        Instant now = Instant.now();

        HitResult result = new HitResult(point, hit, now, execTimeNs);
        repository.add(result);

        return new Response(200, HTML, renderPage(result, repository.findAll(), now));
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