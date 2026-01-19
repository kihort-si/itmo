package ru.itmo.se.is.cw.exception;

import org.springframework.http.HttpStatus;
import ru.itmo.se.is.cw.model.value.ErrorCode;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String message) {
        super(
                HttpStatus.NOT_FOUND,
                "Entity not found",
                ErrorCode.NOT_FOUND,
                message
        );
    }
}
