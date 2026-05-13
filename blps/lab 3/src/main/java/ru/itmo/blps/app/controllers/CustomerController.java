package ru.itmo.blps.app.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.blps.app.models.Customer;
import ru.itmo.blps.app.security.AuthPrincipal;
import ru.itmo.blps.app.services.CustomerService;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Профиль покупателя", description = "Просмотр профиля аутентифицированного покупателя")
@SecurityRequirement(name = "basicAuth")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE_READ')")
    @Operation(
            summary = "Мой профиль",
            description = "Возвращает данные текущего аутентифицированного покупателя. Доступно только роли CUSTOMER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Профиль покупателя",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Требуется авторизация\"}"))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав (не покупатель)",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ запрещен\"}")))
            }
    )
    public Customer me(@AuthenticationPrincipal AuthPrincipal principal) {
        return customerService.getById(principal.getParticipantId());
    }
}
