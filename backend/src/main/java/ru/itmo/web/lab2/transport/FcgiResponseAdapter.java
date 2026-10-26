package ru.itmo.web.lab2.transport;

import ru.itmo.web.lab2.http.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Output adapter: writes a Response to stdout as raw HTTP (Apache runs us with -nph).
 */
public class FcgiResponseAdapter {

    public void write(Response response) throws IOException {
        byte[] bodyBytes = response.body().getBytes(StandardCharsets.UTF_8);
        String head = "HTTP/1.1 " + response.status() + " " + reasonPhrase(response.status()) + "\r\n"
                + "Content-Type: " + response.contentType() + "\r\n"
                + "Content-Length: " + bodyBytes.length + "\r\n"
                + "\r\n";
        System.out.write(head.getBytes(StandardCharsets.UTF_8));
        System.out.write(bodyBytes);
        System.out.flush();
    }


    private String reasonPhrase(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            default -> "Internal Server Error";
        };
    }
}
