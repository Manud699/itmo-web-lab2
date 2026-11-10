package ru.itmo.web.lab2.bootstrap;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.command.CheckPointCommand;
import ru.itmo.web.lab2.command.ClearResultsCommand;
import ru.itmo.web.lab2.command.GetResultsCommand;
import ru.itmo.web.lab2.command.LogoutCommand;
import ru.itmo.web.lab2.command.ResultJsonWriter;
import ru.itmo.web.lab2.domain.AreaChecker;
import ru.itmo.web.lab2.domain.PointValidator;
import ru.itmo.web.lab2.http.ErrorHandler;
import ru.itmo.web.lab2.http.Json;
import ru.itmo.web.lab2.http.JsonErrorHandler;
import ru.itmo.web.lab2.http.Router;
import ru.itmo.web.lab2.repository.InMemoryResultRepository;
import ru.itmo.web.lab2.repository.ResultRepository;
import ru.itmo.web.lab2.session.SessionCookies;
import ru.itmo.web.lab2.session.SessionManager;
import ru.itmo.web.lab2.transport.FcgiRequestAdapter;
import ru.itmo.web.lab2.transport.FcgiResponseAdapter;
import ru.itmo.web.lab2.transport.FcgiServer;

import java.time.Clock;
import java.time.Duration;

/**
 * Creates every object once and connects them through constructors.
 * This is the only place that knows the concrete classes.
 */
public class Bootstrapper {

    private static final long DEFAULT_SESSION_TIMEOUT_SECONDS = 1800;

    public FcgiServer createServer() {
        Json json = new Json();
        ErrorHandler errorHandler = new JsonErrorHandler(json);
        ResultRepository repository = new InMemoryResultRepository();

        Duration sessionTimeout = Duration.ofSeconds(
                Long.getLong("session.timeout.seconds", DEFAULT_SESSION_TIMEOUT_SECONDS));
        SessionManager sessionManager =
                new SessionManager(sessionTimeout, Clock.systemUTC(), repository::clear);
        SessionCookies sessionCookies = new SessionCookies(sessionTimeout);

        Router router = createRouter(json, repository, sessionManager, sessionCookies);

        return new FcgiServer(
                new FcgiRequestAdapter(),
                errorHandler,
                router,
                new FcgiResponseAdapter(),
                new FCGIInterface(),
                sessionManager,
                sessionCookies);
    }

    private Router createRouter(Json json, ResultRepository repository,
                                SessionManager sessionManager, SessionCookies sessionCookies) {
        ResultJsonWriter resultJsonWriter = new ResultJsonWriter(json);
        PointValidator validator = new PointValidator();
        AreaChecker areaChecker = new AreaChecker();

        Router router = new Router();
        router.register("POST", "/check",
                new CheckPointCommand(validator, areaChecker, repository, resultJsonWriter));
        router.register("GET", "/results",
                new GetResultsCommand(resultJsonWriter, repository));
        router.register("POST", "/clear",
                new ClearResultsCommand(resultJsonWriter, repository));
        router.register("POST", "/logout",
                new LogoutCommand(sessionManager, sessionCookies));
        return router;
    }
}