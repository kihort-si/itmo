package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.itmo.se.is.cw.dto.FileMetadataResponseDto;
import ru.itmo.se.is.cw.dto.FileVersionResponseDto;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.service.FilesService;

import java.io.InputStream;
import java.util.List;


@RestController
@RequestMapping("/files")
@Tag(name = "Files", description = "Операции с файлами")
public class FilesController {


    private final FilesService filesService;

    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Загрузка нового файла",
            description = "Загружает новый файл в систему и возвращает его метаданные."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Файл загружен",
                    content = @Content(
                            schema = @Schema(implementation = FileMetadataResponseDto.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_files.write')")
    public ResponseEntity<FileMetadataResponseDto> uploadFile(
            @Parameter(description = "Загружаемый файл", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        FileMetadataResponseDto dto = filesService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Получить метаданные файла",
            description = "Возвращает метаданные файла по его идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Метаданные файла",
                    content = @Content(
                            schema = @Schema(implementation = FileMetadataResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Файл не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<FileMetadataResponseDto> getFileMetadata(
            @PathVariable @Parameter(description = "Идентификатор файла", required = true) Long id
    ) {
        return ResponseEntity.ok(filesService.getFileMetadata(id));
    }


    @GetMapping(
            value = "/{id}/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Operation(
            summary = "Скачать файл",
            description = "Возвращает бинарное содержимое файла для скачивания."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бинарное содержимое файла"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Файл не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @PathVariable @Parameter(description = "Идентификатор файла", required = true) Long id
    ) {
        FileMetadataResponseDto meta = filesService.getFileMetadata(id);
        InputStream is = filesService.downloadFileStream(id);

        StreamingResponseBody body = outputStream -> {
            try (is) {
                is.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }


    @GetMapping("/{id}/versions")
    @Operation(
            summary = "Список версий файла",
            description = "Возвращает список всех доступных версий файла."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Версии файла",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = FileVersionResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Файл не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_files.read')")
    public ResponseEntity<List<FileVersionResponseDto>> getFileVersions(
            @PathVariable @Parameter(description = "Идентификатор файла", required = true) Long id
    ) {
        return ResponseEntity.ok(filesService.getFileVersions(id));
    }


    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Загрузить новую версию файла",
            description = "Добавляет новую версию существующего файла."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Новая версия создана",
                    content = @Content(
                            schema = @Schema(implementation = FileVersionResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Файл не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_files.write')")
    public ResponseEntity<FileVersionResponseDto> uploadNewFileVersion(
            @PathVariable @Parameter(description = "Идентификатор файла", required = true) Long id,

            @Parameter(description = "Новая версия файла", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        FileVersionResponseDto v = filesService.uploadNewFileVersion(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(v);
    }


    @GetMapping(
            value = "/{id}/versions/{versionId}/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Operation(
            summary = "Скачать конкретную версию файла",
            description = "Возвращает бинарное содержимое указанной версии файла."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бинарное содержимое версии"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Файл или версия не найдены",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_files.read')")
    public ResponseEntity<StreamingResponseBody> downloadFileVersion(
            @PathVariable @Parameter(description = "Идентификатор файла", required = true) Long id,
            @PathVariable @Parameter(description = "Идентификатор версии файла", required = true) Long versionId
    ) {
        InputStream is = filesService.downloadFileVersionStream(id, versionId);

        StreamingResponseBody body = outputStream -> {
            try (is) {
                is.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete файла", description = "Помечает файл удалённым (deletedAt), физически не удаляет из хранилища.")
    @PreAuthorize("hasAuthority('SCOPE_files.delete')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        filesService.softDeleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
