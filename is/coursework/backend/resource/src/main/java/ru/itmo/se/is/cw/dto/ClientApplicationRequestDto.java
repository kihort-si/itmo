package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос на создание клиентской заявки")
public class ClientApplicationRequestDto {
    @Schema(description = "Описание заявки", example = "Необходимо изготовить 10 столов")
    private String description;

    @Schema(description = "Количество", example = "10")
    private Integer amount;

    @Schema(description = "Уникальный идентификатор шаблона дизайна (если есть)", example = "601", nullable = true)
    private Long templateProductDesignId;

    @Schema(description = "Уникальный идентификатор товара каталога (если есть)", example = "501", nullable = true)
    private Long catalogProductId;

    @Schema(description = "Список идентификаторов файлов-вложений", example = "[201, 202]")
    private List<Long> attachmentFileIds;
}
