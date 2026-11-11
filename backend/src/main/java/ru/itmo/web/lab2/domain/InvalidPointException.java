package ru.itmo.web.lab2.domain;

public class InvalidPointException extends RuntimeException {

    public InvalidPointException(String message) {
        super(message);
    }
}
