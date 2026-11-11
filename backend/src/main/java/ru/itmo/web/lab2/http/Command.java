package ru.itmo.web.lab2.http;

//Handles one kind of request
@FunctionalInterface
public interface Command {
    Response execute(Request request);
}
