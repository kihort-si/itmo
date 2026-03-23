package ru.itmo.blps.app.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "Bearer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Профиль текущего покупателя", description = "Возвращает данные текущего авторизованного покупателя. Требуется Bearer-токен с ролью CUSTOMER.")
    public Customer me(@AuthenticationPrincipal AuthPrincipal principal) {
        return customerService.getById(principal.getParticipantId());
    }
}
