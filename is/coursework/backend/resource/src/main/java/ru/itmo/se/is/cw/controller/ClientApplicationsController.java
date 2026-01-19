package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.itmo.se.is.cw.dto.*;
import ru.itmo.se.is.cw.dto.filter.ClientApplicationFilter;
import ru.itmo.se.is.cw.service.ClientApplicationsService;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/client-applications")
@Tag(name = "ClientApplications", description = "Операции с клиентскими заявками")
@RequiredArgsConstructor
public class ClientApplicationsController {

    private final ClientApplicationsService clientApplicationsService;

    @PostMapping
    @Operation(
            summary = "Создание клиентской заявки",
            description = "Создает новую клиентскую заявку, включая описание проблемы, пожелания и дополнительные данные."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заявка успешно создана",
                    content = @Content(
                            schema = @Schema(implementation = ClientApplicationResponseDto.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_applications.write')")
    public ResponseEntity<ClientApplicationResponseDto> createApplication(
            @RequestBody ClientApplicationRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        ClientApplicationResponseDto response = clientApplicationsService.createApplication(request);
        URI location = uriBuilder
                .path("//client-applications/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }


    @GetMapping
    @Operation(
            summary = "Список клиентских заявок (пагинация + фильтр)",
            description = """
                    Возвращает список заявок.
                    Для роли CLIENT возвращаются только заявки текущего клиента (filter.clientId игнорируется).
                    Для остальных ролей можно смотреть все заявки и фильтровать по clientId.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Страница заявок",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_applications.read')")
    public ResponseEntity<Page<ClientApplicationResponseDto>> getApplications(
            @ParameterObject @ModelAttribute ClientApplicationFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(clientApplicationsService.getApplications(pageable, filter));
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Детали клиентской заявки",
            description = "Возвращает полную информацию о заявке по её идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Заявка найдена",
                    content = @Content(
                            schema = @Schema(implementation = ClientApplicationResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_applications.read')")
    public ResponseEntity<ClientApplicationResponseDto> getApplicationById(
            @PathVariable @Parameter(description = "Идентификатор заявки", required = true) Long id
    ) {
        ClientApplicationResponseDto response = clientApplicationsService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{id}/attachments")
    @Operation(
            summary = "Добавить вложение к заявке",
            description = "Добавляет файл или ссылку на файл в клиентскую заявку."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Вложение добавлено"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка или файл не найдены",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_applications.attachments.write')")
    public ResponseEntity<Void> addAttachmentToApplication(
            @PathVariable @Parameter(description = "Идентификатор заявки", required = true) Long id,

            @RequestBody AddAttachmentRequestDto request
    ) {
        clientApplicationsService.addAttachmentToApplication(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/{id}/attachments")
    @Operation(
            summary = "Список вложений заявки",
            description = "Возвращает список всех вложенных файлов для указанной заявки."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список вложений",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = FileMetadataResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_applications.attachments.read')")
    public ResponseEntity<List<FileMetadataResponseDto>> getApplicationAttachments(
            @PathVariable @Parameter(description = "Идентификатор заявки", required = true) Long id
    ) {
        return ResponseEntity.ok(clientApplicationsService.getApplicationAttachments(id));
    }
}
