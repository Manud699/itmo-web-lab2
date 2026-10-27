package ru.itmo.web.lab2;

import ru.itmo.web.lab2.bootstrap.Bootstrapper;
import ru.itmo.web.lab2.transport.FcgiServer;

public class AppMain {

    public static void main(String[] args) {
        FcgiServer server = new Bootstrapper().createServer();
        server.start();
    }
}