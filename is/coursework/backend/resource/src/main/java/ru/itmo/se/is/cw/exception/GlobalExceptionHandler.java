package ru.itmo.se.is.cw.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.model.value.ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex) {
        ProblemDetail pd = ex.toProblemDetail();
        log.debug("Business error: {}", ex.getMessage());
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ProblemDetail> handleAccessDenied(Exception ex) {
        ProblemDetail pd = ProblemDetail.builder()
                .title("Forbidden")
                .detail("Access denied")
                .status(403)
                .code(ErrorCode.FORBIDDEN)
                .build();

        log.debug("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(pd.getStatus())).body(pd);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuth(AuthenticationException ex) {
        ProblemDetail pd = ProblemDetail.builder()
                .title("Unauthorized")
                .detail("Access denied")
                .status(401)
                .code(ErrorCode.UNAUTHORIZED)
                .build();

        log.debug("Unauthorized: {}", ex.getMessage());
        pd.setStatus(401);
        pd.setTitle("Unauthorized");
        pd.setDetail(ex.getMessage());
        return ResponseEntity.status(401).body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        ValidationException be = new ValidationException(ex.getConstraintViolations());
        ProblemDetail pd = be.toProblemDetail();
        log.debug("Constraint violation exception: {}", ex.getMessage());
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        ValidationException be = ValidationException.fromBindingResult(ex.getBindingResult());
        ProblemDetail pd = be.toProblemDetail();
        log.debug("Request body validation failed: {}", ex.getMessage());
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException ex) {
        ValidationException be = ValidationException.fromBindingResult(ex.getBindingResult());
        ProblemDetail pd = be.toProblemDetail();
        log.debug("Binding/validation failed: {}", ex.getMessage());
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleInvalidJson(HttpMessageNotReadableException ex) {
        ex.getMostSpecificCause();
        InvalidJsonException be = new InvalidJsonException(
                ex.getMostSpecificCause().getMessage() != null
                        ? ex.getMostSpecificCause().getMessage()
                        : "Invalid JSON",
                ex
        );
        ProblemDetail pd = be.toProblemDetail();
        log.debug("Json parse exception: {}", ex.getMessage());
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ProblemDetail> handleAny(Throwable ex) {
        log.warn("Internal server error: {}", ex.getMessage(), ex);

        ProblemDetail pd = ProblemDetail.builder()
                .title("Unknown error")
                .detail(ex.getMessage())
                .status(500)
                .code(ErrorCode.INTERNAL_SERVER_ERROR)
                .build();

        return ResponseEntity.status(HttpStatusCode.valueOf(pd.getStatus())).body(pd);
    }
}
