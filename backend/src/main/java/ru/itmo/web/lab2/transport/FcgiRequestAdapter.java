package ru.itmo.web.lab2.transport;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.http.HttpException;
import ru.itmo.web.lab2.http.Request;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Input adapter: turns the current FastCGI request (params + stdin) into a Request.
 */
public class FcgiRequestAdapter {

    public Request readRequest() throws IOException {
        String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD", "GET");
        String path = FCGIInterface.request.params.getProperty("PATH_INFO", "");
        String body = readBody();
        return new Request(method, path, parseForm(body));
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
