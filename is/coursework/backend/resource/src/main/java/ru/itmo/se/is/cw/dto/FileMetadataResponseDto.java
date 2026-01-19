package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Метаданные файла")
public class FileMetadataResponseDto {
    @Schema(description = "Уникальный идентификатор файла", example = "201")
    private Long id;

    @Schema(description = "Оригинальное имя файла", example = "design.dxf")
    private String filename;

    @Schema(description = "Тип содержимого файла", example = "application/dxf")
    private String contentType;

    @Schema(description = "Размер файла в байтах", example = "102400")
    private Long sizeBytes;

    @Schema(description = "Уникальный идентификатор владельца файла", example = "123")
    private Long ownerId;

    @Schema(description = "Дата и время создания файла", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления файла", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime updatedAt;

    @Schema(description = "Дата и время удаления файла (если удален)", example = "2023-10-20T10:00:00Z", nullable = true)
    private ZonedDateTime deletedAt;
}
