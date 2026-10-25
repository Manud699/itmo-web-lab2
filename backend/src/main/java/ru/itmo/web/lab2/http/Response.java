package ru.itmo.web.lab2.http;
/** What a command answers: status code, content type and body. The transport turns it into bytes. */
public record Response(int status, String contentType, String body) {
}
