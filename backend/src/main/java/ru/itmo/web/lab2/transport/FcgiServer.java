package ru.itmo.web.lab2.transport;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.http.*;
import ru.itmo.web.lab2.session.Session;
import ru.itmo.web.lab2.session.SessionManager;

import java.io.IOException;

/**
 * The FastCGI request loop: read -> route -> write.
 */
public class FcgiServer {

    private final FcgiRequestAdapter requestAdapter;
    private final FcgiResponseAdapter responseAdapter;
    private final FCGIInterface fcgiInterface;
    private final Router router;
    private final ErrorHandler errorHandler;
    private final SessionManager sessionManager;

    public FcgiServer(FcgiRequestAdapter requestAdapter,
                      ErrorHandler errorHandler,
                      Router router,
                      FcgiResponseAdapter responseAdapter,
                      FCGIInterface fcgiInterface,
                      SessionManager sessionManager) {
        this.requestAdapter = requestAdapter;
        this.errorHandler = errorHandler;
        this.router = router;
        this.responseAdapter = responseAdapter;
        this.fcgiInterface = fcgiInterface;
        this.sessionManager = sessionManager;
    }

    public void start() {
        while (fcgiInterface.FCGIaccept() != -1) {
            send(handle());
        }
    }


    private Response handle() {
        try {
            Request request = requestAdapter.readRequest();
            Session session = sessionManager.resolve(request.sessionId());
            return router.handle(request.withSessionId(session.id())).withNewSession(session.id());
        } catch (HttpException e) {
            return errorHandler.handle(e);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return errorHandler.handleUnexpected(e);
        }
    }


    private void send(Response response) {
        try {
            responseAdapter.write(response);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
