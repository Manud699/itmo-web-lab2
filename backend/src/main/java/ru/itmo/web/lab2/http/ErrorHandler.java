package ru.itmo.web.lab2.http;

public interface ErrorHandler {

    Response handle(HttpException httpException);
    Response handleUnexpected(Exception e);

}


