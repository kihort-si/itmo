package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на добавление файла к дизайну")
public class AddFileToDesignRequest {
    @Schema(description = "Уникальный идентификатор файла", example = "501")
    private Long fileId;
}

