package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.se.is.cw.dto.ClientRegistrationRequestDto;
import ru.itmo.se.is.cw.service.ClientsService;

@RestController
@Tag(name = "Auth", description = "Регистрация клиентов")
@RequestMapping("/register")
@RequiredArgsConstructor
public class AuthController {

    private final ClientsService clientsService;

    @PostMapping
    @Operation(
            summary = "Регистрация клиента",
            description = "Регистрирует нового клиента по логину/паролю."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Клиент зарегистрирован (создан)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса / нарушены бизнес-правила",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<Void> register(
            @RequestBody ClientRegistrationRequestDto request
    ) {
        clientsService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
