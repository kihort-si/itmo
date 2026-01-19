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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.itmo.se.is.cw.dto.ConversationParticipantResponseDto;
import ru.itmo.se.is.cw.dto.MessageResponseDto;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.dto.SendMessageRequestDto;
import ru.itmo.se.is.cw.dto.filter.MessageFilter;
import ru.itmo.se.is.cw.service.ConversationsService;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/conversations")
@Tag(name = "Conversations", description = "Операции с диалогами")
@RequiredArgsConstructor
public class ConversationsController {


    private final ConversationsService conversationsService;

    @GetMapping("/{id}/messages")
    @Operation(
            summary = "Сообщения диалога",
            description = "Возвращает список сообщений диалога с момента указанной даты."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список сообщений",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = MessageResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Диалог не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_conversations.messages.read')")
    public ResponseEntity<Page<MessageResponseDto>> getMessages(
            @PathVariable @Parameter(description = "Идентификатор диалога", required = true) Long id,
            @ParameterObject @ModelAttribute MessageFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<MessageResponseDto> messages = conversationsService.getMessages(id, pageable, filter);
        return ResponseEntity.ok(messages);
    }


    @PostMapping("/{id}/messages")
    @Operation(
            summary = "Отправить сообщение в диалог",
            description = "Добавляет новое сообщение в указанный диалог."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Сообщение отправлено",
                    content = @Content(
                            schema = @Schema(implementation = MessageResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Диалог не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_conversations.messages.write')")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @PathVariable @Parameter(description = "Идентификатор диалога", required = true) Long id,
            @RequestBody SendMessageRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        MessageResponseDto message = conversationsService.sendMessage(id, request);
        URI location = uriBuilder
                .path("/conversation/{id}/messages/{messageId}")
                .buildAndExpand(id, message.getId())
                .toUri();
        return ResponseEntity.created(location).body(message);
    }


    @GetMapping("/{id}/participants")
    @Operation(
            summary = "Участники диалога",
            description = "Возвращает список всех участников указанного диалога."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список участников диалога",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ConversationParticipantResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Диалог не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_conversations.patricipants.read')")
    public ResponseEntity<List<ConversationParticipantResponseDto>> getParticipants(
            @PathVariable @Parameter(description = "Идентификатор диалога", required = true) Long id
    ) {
        return ResponseEntity.ok(conversationsService.getParticipants(id));
    }
}
