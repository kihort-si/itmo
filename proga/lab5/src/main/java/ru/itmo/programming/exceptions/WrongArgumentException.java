package ru.itmo.programming.exceptions;

/**
 * @author Nikita Vasilev
 */
public class WrongArgumentException extends Exception {
    public WrongArgumentException(String message) {
        super(message);
    }
}
