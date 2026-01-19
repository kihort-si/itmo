package ru.itmo.se.is.cw.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.model.value.ErrorCode;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String title;
    private final ErrorCode code;

    protected BusinessException(HttpStatus status,
                                String title,
                                ErrorCode code,
                                String message) {
        super(message);
        this.status = status;
        this.title = title;
        this.code = code;
    }

    protected BusinessException(HttpStatus status,
                                String title,
                                ErrorCode code,
                                String message,
                                Throwable cause) {
        super(message, cause);
        this.status = status;
        this.title = title;
        this.code = code;
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail problemDetail = ProblemDetail.builder()
                .title(title)
                .detail(getMessage())
                .status(status.value())
                .code(code)
                .build();

        enrichProblemDetail(problemDetail);
        return problemDetail;
    }

    protected void enrichProblemDetail(ProblemDetail problemDetail) {
    }
}
