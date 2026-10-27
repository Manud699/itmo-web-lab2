package ru.itmo.web.lab2.http;

/** Thrown anywhere while handling a request to stop it with an HTTP error status (400, 404, 405...). */
public class HttpException extends RuntimeException {

    private final int status;

    public HttpException(int status, String message){
        super(message);
        this.status = status;
    }

    public int getStatus(){
        return status;
    }
}
