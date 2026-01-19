package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Версия файла")
public class FileVersionResponseDto {
    @Schema(description = "Уникальный идентификатор версии", example = "301")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанного файла", example = "201")
    private Long fileId;

    @Schema(description = "Уникальный идентификатор создателя версии", example = "101")
    private Long creatorId;

    @Schema(description = "Имя бакета хранения", example = "file-bucket")
    private String bucket;

    @Schema(description = "Ключ объекта в хранилище", example = "files/201/version_301.dxf")
    private String objectKey;

    @Schema(description = "Размер файла в байтах", example = "105000")
    private Long sizeBytes;

    @Schema(description = "Тип содержимого файла", example = "application/dxf")
    private String contentType;

    @Schema(description = "Дата и время загрузки версии", example = "2023-10-20T10:00:00Z")
    private ZonedDateTime uploadedAt;
}
