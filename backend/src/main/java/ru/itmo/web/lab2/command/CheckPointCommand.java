package ru.itmo.web.lab2.command;

import ru.itmo.web.lab2.domain.*;
import ru.itmo.web.lab2.http.*;
import ru.itmo.web.lab2.repository.ResultRepository;

import java.time.Instant;

public class CheckPointCommand implements Command {

    private final PointValidator validator;
    private final AreaChecker areaChecker;
    private final ResultRepository repository;
    private final ResultJsonWriter resultJsonWriter;

    public CheckPointCommand(PointValidator validator, AreaChecker areaChecker,
                             ResultRepository repository, ResultJsonWriter resultJsonWriter){
        this.validator = validator;
        this.areaChecker = areaChecker;
        this.repository = repository;
        this.resultJsonWriter = resultJsonWriter;
    }

    @Override
    public Response execute(Request request) {
        long time = System.nanoTime();
        Point point;
        try {
            point = validator.validate(request.params());
        } catch (InvalidPointException e){
            throw new HttpException(400, e.getMessage());
        }
        boolean isHit = areaChecker.isHit(point);
        long timeExecutionNs =  System.nanoTime() - time;
        Instant now = Instant.now();
        HitResult hitResult = new HitResult(point, isHit, now, timeExecutionNs);
        repository.add(request.sessionId(), hitResult);
        return new Response(200, Json.CONTENT_TYPE, resultJsonWriter.writeCheckResponse(hitResult, repository.findAll(request.sessionId()), now));
    }
}
