package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на добавление вложения к заявке")
public class AddAttachmentRequestDto {
    @Schema(description = "Уникальный идентификатор файла", example = "201")
    private Long fileId;
}
