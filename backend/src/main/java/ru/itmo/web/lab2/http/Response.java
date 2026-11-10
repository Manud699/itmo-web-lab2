package ru.itmo.web.lab2.http;
/** What a command answers: status code, content type, body and the cookie to set (null if none). The transport turns it into bytes. */
public record Response(int status, String contentType, String body, SetCookie cookie) {
    public Response(int status, String contentType, String body){
        this(status, contentType, body, null);
    }
    public Response withCookie(SetCookie cookie){
        return new Response(status, contentType, body, cookie);
    }
}
