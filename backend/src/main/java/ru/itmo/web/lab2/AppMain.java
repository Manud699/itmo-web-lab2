package ru.itmo.web.lab2;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.command.CheckPointCommand;
import ru.itmo.web.lab2.command.ResultJsonWriter;
import ru.itmo.web.lab2.domain.AreaChecker;
import ru.itmo.web.lab2.domain.PointValidator;
import ru.itmo.web.lab2.http.*;
import ru.itmo.web.lab2.repository.InMemoryResultRepository;
import ru.itmo.web.lab2.repository.ResultRepository;
import ru.itmo.web.lab2.transport.FcgiRequestAdapter;
import ru.itmo.web.lab2.transport.FcgiResponseAdapter;
import ru.itmo.web.lab2.transport.FcgiServer;

public class AppMain {

    public static void main(String[] args) {
        Json json = new Json();
        ErrorHandler errorHandler = new JsonErrorHandler(json);
        ResultJsonWriter resultJsonWriter = new ResultJsonWriter(json);
        PointValidator validator = new PointValidator();
        AreaChecker areaChecker = new AreaChecker();
        ResultRepository repository = new InMemoryResultRepository();
        Command checkPointCommand = new CheckPointCommand(validator, areaChecker, repository, resultJsonWriter);

        Router router = new Router();
        router.register("POST", "/check", checkPointCommand);

        FcgiServer server = new FcgiServer(
                new FcgiRequestAdapter(),
                errorHandler,
                router,
                new FcgiResponseAdapter(),
                new FCGIInterface());
        server.start();
    }
}
