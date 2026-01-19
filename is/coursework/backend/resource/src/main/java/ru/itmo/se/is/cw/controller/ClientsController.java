package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.itmo.se.is.cw.dto.ClientResponseDto;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.dto.filter.ClientFilter;
import ru.itmo.se.is.cw.service.ClientsService;


@RestController
@RequestMapping("/clients")
@Tag(name = "Clients", description = "Операции с клиентами")
@RequiredArgsConstructor
public class ClientsController {

    private final ClientsService clientsService;

    @GetMapping
    @Operation(
            summary = "Список клиентов",
            description = "Возвращает список всех клиентов. Доступно администраторам и менеджерам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список клиентов"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный параметр запроса",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_clients.read')")
    public ResponseEntity<Page<ClientResponseDto>> getClients(
            @ParameterObject @ModelAttribute ClientFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ClientResponseDto> clients = clientsService.getClients(pageable, filter);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Детали клиента",
            description = "Возвращает информацию о клиенте по его идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о клиенте",
                    content = @Content(
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Клиент не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_clients.read')")
    public ResponseEntity<ClientResponseDto> getClientById(
            @PathVariable @Parameter(description = "Идентификатор клиента", required = true) Long id
    ) {
        ClientResponseDto client = clientsService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/by-account/{accountId}")
    @Operation(
            summary = "Получение клиента по accountId",
            description = "Возвращает информацию о клиенте по идентификатору аккаунта."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о клиенте",
                    content = @Content(
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Клиент не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_clients.read')")
    public ResponseEntity<ClientResponseDto> getClientByAccountId(
            @PathVariable @Parameter(description = "Идентификатор аккаунта", required = true) Long accountId
    ) {
        ClientResponseDto client = clientsService.getClientByAccountId(accountId);
        return ResponseEntity.ok(client);
    }
}
