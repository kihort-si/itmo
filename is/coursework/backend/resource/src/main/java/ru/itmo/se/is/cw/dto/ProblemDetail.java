package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.*;
import ru.itmo.se.is.cw.model.value.ErrorCode;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProblemDetail",
        description = "Стандартизированное описание ошибки (RFC 7807-стиль) с дополнительным кодом и произвольными свойствами."
)
public class ProblemDetail {

    @Schema(
            description = "Короткий заголовок ошибки",
            example = "Validation failed"
    )
    private String title;

    @Schema(
            description = "HTTP статус-код",
            example = "400",
            minimum = "100",
            maximum = "599"
    )
    private int status;

    @Schema(
            description = "Подробное описание ошибки",
            example = "Field 'username' must not be blank"
    )
    private String detail;

    @Schema(
            description = "Код ошибки приложения",
            example = "VALIDATION_ERROR"
    )
    private ErrorCode code;

    @Nullable
    @Schema(
            description = "Дополнительные произвольные свойства (например, fieldErrors, traceId и т.п.)",
            example = """
                    {
                      "traceId": "a1b2c3d4",
                      "fieldErrors": {
                        "username": "must not be blank",
                        "password": "must be at least 8 characters"
                      }
                    }
                    """
    )
    private Map<String, Object> properties;

    public void setProperty(String name, @Nullable Object value) {
        this.properties = (this.properties != null ? this.properties : new LinkedHashMap<>());
        this.properties.put(name, value);
    }
}
