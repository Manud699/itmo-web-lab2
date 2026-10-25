package ru.itmo.web.lab2;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.command.CheckPointCommand;
import ru.itmo.web.lab2.command.ResultJsonWriter;
import ru.itmo.web.lab2.domain.AreaChecker;
import ru.itmo.web.lab2.domain.PointValidator;
import ru.itmo.web.lab2.http.*;
import ru.itmo.web.lab2.repository.InMemoryResultRepository;
import ru.itmo.web.lab2.repository.ResultRepository;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AppMain {

    private static final String CONTENT_LENGTH = "CONTENT_LENGTH";

    public static void main(String[] args) {
        var fastCGIJava = new FCGIInterface();
        Json json = new Json();
        ErrorHandler errorHandler = new JsonErrorHandler(json);
        ResultJsonWriter resultJsonWriter= new ResultJsonWriter(json);
        PointValidator validator = new PointValidator();
        AreaChecker areaChecker = new AreaChecker();
        ResultRepository repository = new InMemoryResultRepository();
        Router router = new Router();
        Command checkPointCommand = new CheckPointCommand(validator, areaChecker, repository, resultJsonWriter);
        router.register("POST", "/check", checkPointCommand);
        while (fastCGIJava.FCGIaccept() >= 0) {
            try {
                Request request = readRequest();
                Response response = router.handle(request);
                sendResponse(response);

            } catch (HttpException e) {
                sendResponse(errorHandler.handle(e));

            } catch (Exception e) {
                e.printStackTrace(System.err);
                sendResponse(errorHandler.handleUnexpected(e));
            }
        }
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