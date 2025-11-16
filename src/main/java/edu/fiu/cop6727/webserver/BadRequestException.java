package edu.fiu.cop6727.webserver;

/**
 * Signals an HTTP 400 Bad Request condition while parsing input.
 */
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
