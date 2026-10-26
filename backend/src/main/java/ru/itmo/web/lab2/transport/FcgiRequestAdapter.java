package ru.itmo.web.lab2.transport;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.http.HttpException;
import ru.itmo.web.lab2.http.Request;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Input adapter: turns the current FastCGI request (params + stdin) into a Request.
 */
public class FcgiRequestAdapter {

    public static final String SESSION_COOKIE = "SESSIONID";
    private static final Pattern UUID_FORMAT =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    public Request readRequest() throws IOException {
        String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD", "GET");
        String path = FCGIInterface.request.params.getProperty("PATH_INFO", "");
        String body = readBody();
        String sessionId = readSessionId(FCGIInterface.request.params.getProperty("HTTP_COOKIE", ""));
        return new Request(method, path, parseForm(body), sessionId);
    }


    private String readSessionId(String cookieHeader) {
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && parts[0].equals(SESSION_COOKIE)) {
                return UUID_FORMAT.matcher(parts[1]).matches() ? parts[1] : null;
            }
        }
        return null;
    }


    private String readBody() throws IOException {
        String lengthText = FCGIInterface.request.params.getProperty("CONTENT_LENGTH", "0");
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


    private Map<String, String> parseForm(String body) {
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
}
