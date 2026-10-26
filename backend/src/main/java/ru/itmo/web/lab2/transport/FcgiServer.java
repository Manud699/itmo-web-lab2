package ru.itmo.web.lab2.transport;

import com.fastcgi.FCGIInterface;
import ru.itmo.web.lab2.http.*;

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

    public FcgiServer(FcgiRequestAdapter requestAdapter,
                      ErrorHandler errorHandler,
                      Router router,
                      FcgiResponseAdapter responseAdapter,
                      FCGIInterface fcgiInterface) {
        this.requestAdapter = requestAdapter;
        this.errorHandler = errorHandler;
        this.router = router;
        this.responseAdapter = responseAdapter;
        this.fcgiInterface = fcgiInterface;
    }

    public void start() {
        while (fcgiInterface.FCGIaccept() != -1) {
            send(handle());
        }
    }


    private Response handle() {
        try {
            Request request = requestAdapter.readRequest();
            return router.handle(request);
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
