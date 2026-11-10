package ru.itmo.web.lab2.command;

import ru.itmo.web.lab2.domain.HitResult;
import ru.itmo.web.lab2.http.Command;
import ru.itmo.web.lab2.http.Json;
import ru.itmo.web.lab2.http.Request;
import ru.itmo.web.lab2.http.Response;
import ru.itmo.web.lab2.repository.ResultRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** GET /admin/highest: the point with the greatest Y among the points of all sessions. */
public class HighestPointCommand implements Command {

    private final ResultRepository repository;
    private final ResultJsonWriter jsonWriter;

    public HighestPointCommand(ResultRepository repository, ResultJsonWriter jsonWriter) {
        this.repository = repository;
        this.jsonWriter = jsonWriter;
    }

    @Override
    public Response execute(Request request) {
        List<HitResult> all = repository.findAllSessions();
        Optional<HitResult> highest = all.stream()
                .max(Comparator.comparing(r -> r.point().y()));
        String body = jsonWriter.writeHighestResponse(highest, all.size(), Instant.now());
        return new Response(200, Json.CONTENT_TYPE, body);
    }
}