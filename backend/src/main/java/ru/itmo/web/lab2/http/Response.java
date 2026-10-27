package ru.itmo.web.lab2.http;
/** What a command answers: status code, content type, body and the session id to send as a cookie . The transport turns it into bytes. */
public record Response(int status, String contentType, String body, String newSessionId) {
    public Response(int status, String contentType, String body){
        this(status, contentType, body, null);
    }
    public Response withNewSession(String sessionId){
        return new Response(status, contentType, body, sessionId);
    }
}
