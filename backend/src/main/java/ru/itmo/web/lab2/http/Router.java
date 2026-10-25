package ru.itmo.web.lab2.http;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, Map<String, Command>> routes = new HashMap<>();

    public void register(String method,String path, Command command){
            routes.computeIfAbsent(path, p -> new HashMap<>()).put(method, command);
    }

    public Response handle(Request request){
         String path = request.path();
         Map<String, Command> commandsByMethod = routes.get(path);
         if(commandsByMethod == null) throw new HttpException(404,"Not found: " + path);
         Command command = commandsByMethod.get(request.method());
         if(command == null) throw new HttpException(405,"Method " + request.method() + " is not allowed for " + request.path());
         return command.execute(request);
    }
}
