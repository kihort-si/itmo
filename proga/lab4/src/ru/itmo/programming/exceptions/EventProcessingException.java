package ru.itmo.programming.exceptions;

public class EventProcessingException extends Exception {
    private String object;
    private String message;

    public EventProcessingException(String object, String message) {
        super(object + message);
        this.object = object;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return object + message;
    }
}
