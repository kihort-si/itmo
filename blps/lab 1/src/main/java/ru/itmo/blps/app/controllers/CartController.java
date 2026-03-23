package ru.itmo.blps.app.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.blps.app.dto.AddCartItemRequest;
import ru.itmo.blps.app.dto.CartResponse;
import ru.itmo.blps.app.dto.UpdateCartItemRequest;
import ru.itmo.blps.app.exceptions.BusinessException;
import ru.itmo.blps.app.security.AuthPrincipal;
import ru.itmo.blps.app.services.CartService;

@RestController
@RequestMapping("/api/customers/{customerId}/cart")
@SecurityRequirement(name = "Bearer")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Просмотреть корзину", description = "Возвращает содержимое корзины покупателя. Требуется Bearer-токен с ролью CUSTOMER.")
    public CartResponse getCart(@PathVariable Long customerId, @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.getOrCreateCart(customerId);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Добавить товар в корзину", description = "Добавляет товар в корзину покупателя. Требуется Bearer-токен с ролью CUSTOMER.")
    public CartResponse addItem(@PathVariable Long customerId,
                                @RequestBody @Valid AddCartItemRequest request,
                                @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.addItem(customerId, request.productId(), request.quantity());
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Изменить количество товара", description = "Обновляет количество позиции в корзине. Требуется Bearer-токен с ролью CUSTOMER.")
    public CartResponse updateItem(@PathVariable Long customerId,
                                   @PathVariable Long itemId,
                                   @RequestBody @Valid UpdateCartItemRequest request,
                                   @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.updateItem(customerId, itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Удалить товар из корзины", description = "Удаляет позицию из корзины покупателя. Требуется Bearer-токен с ролью CUSTOMER.")
    public CartResponse removeItem(@PathVariable Long customerId,
                                   @PathVariable Long itemId,
                                   @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.removeItem(customerId, itemId);
    }

    @PostMapping("/clear")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Очистить корзину", description = "Удаляет все товары из корзины. Требуется Bearer-токен с ролью CUSTOMER.")
    public CartResponse clear(@PathVariable Long customerId, @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.clear(customerId);
    }

    private void ensureOwnCustomer(Long customerId, AuthPrincipal principal) {
        if (!principal.getParticipantId().equals(customerId)) {
            throw new BusinessException("Доступ к чужой корзине запрещен");
        }
    }
}
