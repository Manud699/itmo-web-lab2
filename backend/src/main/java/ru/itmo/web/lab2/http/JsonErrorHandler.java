package ru.itmo.web.lab2.http;

public class JsonErrorHandler implements ErrorHandler {
    private final Json json;

    public JsonErrorHandler(Json json) {
        this.json = json;
    }

    @Override
    public Response handle(HttpException httpException) {
        return error(httpException.getStatus(), httpException.getMessage());
    }

    @Override
    public Response handleUnexpected(Exception e){
        return error(500, "Internal server error");
    }

    private Response error(int status, String message){
        return new Response(status, Json.CONTENT_TYPE, "{\"error\":" + json.quote(message) + "}");
    }
}
