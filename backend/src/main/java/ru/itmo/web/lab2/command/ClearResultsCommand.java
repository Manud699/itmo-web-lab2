package ru.itmo.web.lab2.command;

import ru.itmo.web.lab2.http.Command;
import ru.itmo.web.lab2.http.Json;
import ru.itmo.web.lab2.http.Request;
import ru.itmo.web.lab2.http.Response;
import ru.itmo.web.lab2.repository.ResultRepository;

import java.time.Instant;

/** POST /clear: deletes the history of the current session and returns it. */
public class ClearResultsCommand implements Command {

    private final ResultJsonWriter jsonWriter;
    private final ResultRepository repository;

    public ClearResultsCommand(ResultJsonWriter jsonWriter, ResultRepository repository) {
        this.jsonWriter = jsonWriter;
        this.repository = repository;
    }

    @Override
    public Response execute(Request request) {
        repository.clear(request.sessionId());
        String body = jsonWriter.writeResultsResponse(repository.findAll(request.sessionId()), Instant.now());
        return new Response(200, Json.CONTENT_TYPE, body);
    }
}
