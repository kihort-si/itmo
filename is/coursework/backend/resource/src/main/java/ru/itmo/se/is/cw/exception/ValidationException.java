package ru.itmo.se.is.cw.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.model.value.ErrorCode;

import java.util.List;
import java.util.Set;

@Getter
public class ValidationException extends BusinessException {

    private final List<FieldError> errors;

    public ValidationException(Set<? extends ConstraintViolation<?>> violations) {
        super(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                ErrorCode.VALIDATION_ERROR,
                "Validation failed for " + violations.size() + " field(s)"
        );

        this.errors = violations.stream()
                .map(v -> new FieldError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
    }

    public ValidationException(List<FieldError> errors) {
        super(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                ErrorCode.VALIDATION_ERROR,
                "Validation failed for " + errors.size() + " field(s)"
        );
        this.errors = errors;
    }

    public static ValidationException fromBindingResult(BindingResult br) {
        List<FieldError> errors = br.getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return new ValidationException(errors);
    }

    @Override
    protected void enrichProblemDetail(ProblemDetail problemDetail) {
        problemDetail.setProperty("errors", errors);
    }

    public record FieldError(String field, String message) {
    }
}
