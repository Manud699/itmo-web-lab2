package ru.itmo.web.lab2.http;

import java.util.Map;

/** An HTTP request already read from the transport: method, path and form parameters. */
public record Request(String method, String path, Map<String, String> params) {
    public Request{
        params = Map.copyOf(params);
    }
    public String param(String name){
        return params.get(name);
    }
}
