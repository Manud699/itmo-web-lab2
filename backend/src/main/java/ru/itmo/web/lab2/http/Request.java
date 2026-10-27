package ru.itmo.web.lab2.http;

import java.util.Map;

/** An HTTP request already read from the transport: method, path, form parameters and session id. */
public record Request(String method, String path, Map<String, String> params, String sessionId) {
    public Request{
        params = Map.copyOf(params);
    }
    public String param(String name){
        return params.get(name);
    }
    public Request withSessionId(String sessionId){
        return new Request(method, path, params, sessionId);
    }
}
