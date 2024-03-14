package ru.itmo.programming.exceptions;

/**
 * @author Nikita Vasilev
 */
public class EmptyCollectionException extends Exception {
    public EmptyCollectionException(String message) {
        super(message);
    }
}
