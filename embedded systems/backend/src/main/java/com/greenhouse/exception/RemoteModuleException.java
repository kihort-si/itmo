package com.greenhouse.exception;

public class RemoteModuleException extends RuntimeException {
    public RemoteModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
