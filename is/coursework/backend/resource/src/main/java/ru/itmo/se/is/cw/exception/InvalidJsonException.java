package ru.itmo.se.is.cw.exception;

import org.springframework.http.HttpStatus;
import ru.itmo.se.is.cw.model.value.ErrorCode;

public class InvalidJsonException extends BusinessException {
    public InvalidJsonException(String message, Throwable cause) {
        super(
                HttpStatus.BAD_REQUEST,
                "Invalid JSON",
                ErrorCode.INVALID_JSON,
                message,
                cause
        );
    }
}
