package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Клиентская заявка")
public class ClientApplicationResponseDto {
    @Schema(description = "Уникальный идентификатор заявки", example = "801")
    private Long id;

    @Schema(description = "Уникальный идентификатор клиента", example = "456")
    private Long clientId;

    @Schema(description = "Описание заявки", example = "Необходимо изготовить 10 столов")
    private String description;

    @Schema(description = "Количество", example = "10")
    private Integer amount;

    @Schema(description = "Уникальный идентификатор шаблона дизайна (если есть)", example = "601", nullable = true)
    private Long templateProductDesignId;

    @Schema(description = "Дата и время создания заявки", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;
}
