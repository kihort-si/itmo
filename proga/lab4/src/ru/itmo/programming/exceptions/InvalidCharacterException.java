package ru.itmo.programming.exceptions;

public class InvalidCharacterException extends RuntimeException {
    public InvalidCharacterException(String message, Throwable cause){
        super(message, cause);
    }
}
