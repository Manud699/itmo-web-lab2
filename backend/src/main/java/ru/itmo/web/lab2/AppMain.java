package ru.itmo.web.lab2;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AppMain {
    
    private static final String CONTENT_LENGTH = "CONTENT_LENGTH";

    public static void main(String[] args) {
        var fastCGIJava = new FCGIInterface();

        while (fastCGIJava.FCGIaccept() >= 0) {
            try {
                String body = readBody();
                sendResponse(200, "OK", "Echo: " + body);

            } catch (Exception e) {
                e.printStackTrace(System.err);
                sendResponse(500, "Internal Server Error", "Error interno");
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

    private static void sendResponse(int status, String statusText, String body) {
        try {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            String head = "HTTP/1.1 " + status + " " + statusText + "\r\n"
                    + "Content-Type: text/plain; charset=utf-8\r\n"
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
