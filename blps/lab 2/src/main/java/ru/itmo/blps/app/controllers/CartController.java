package ru.itmo.blps.app.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Корзина", description = "Управление корзиной покупателя. Доступно только роли CUSTOMER для собственной корзины.")
@SecurityRequirement(name = "basicAuth")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CART_MANAGE')")
    @Operation(
            summary = "Получить корзину",
            description = "Возвращает текущую корзину покупателя. Если корзины нет — создаётся пустая.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Корзина покупателя",
                            content = @Content(schema = @Schema(implementation = CartResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Требуется авторизация\"}"))),
                    @ApiResponse(responseCode = "403", description = "Попытка доступа к чужой корзине",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ к чужой корзине запрещен\"}")))
            }
    )
    public CartResponse getCart(
            @Parameter(description = "ID покупателя") @PathVariable Long customerId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.getOrCreateCart(customerId);
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('CART_MANAGE')")
    @Operation(
            summary = "Добавить товар в корзину",
            description = "Добавляет товар в корзину или увеличивает количество, если он уже есть. " +
                    "Проверяет наличие товара на складе.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённая корзина",
                            content = @Content(schema = @Schema(implementation = CartResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Недостаточно товара на складе или ошибка валидации",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Недостаточно товара на складе\"}"))),
                    @ApiResponse(responseCode = "404", description = "Товар не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Товар не найден: id=3\"}")))
            }
    )
    public CartResponse addItem(
            @Parameter(description = "ID покупателя") @PathVariable Long customerId,
            @RequestBody @Valid AddCartItemRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.addItem(customerId, request.productId(), request.quantity());
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('CART_MANAGE')")
    @Operation(
            summary = "Изменить количество товара",
            description = "Устанавливает новое количество для позиции в корзине. Проверяет остатки на складе.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённая корзина",
                            content = @Content(schema = @Schema(implementation = CartResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Недостаточно товара на складе",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Недостаточно товара на складе\"}"))),
                    @ApiResponse(responseCode = "404", description = "Позиция корзины не найдена",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Позиция корзины не найдена: id=7\"}")))
            }
    )
    public CartResponse updateItem(
            @Parameter(description = "ID покупателя") @PathVariable Long customerId,
            @Parameter(description = "ID позиции корзины") @PathVariable Long itemId,
            @RequestBody @Valid UpdateCartItemRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.updateItem(customerId, itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('CART_MANAGE')")
    @Operation(
            summary = "Удалить позицию из корзины",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённая корзина",
                            content = @Content(schema = @Schema(implementation = CartResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Позиция корзины не найдена",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Позиция корзины не найдена: id=7\"}")))
            }
    )
    public CartResponse removeItem(
            @Parameter(description = "ID покупателя") @PathVariable Long customerId,
            @Parameter(description = "ID позиции корзины") @PathVariable Long itemId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.removeItem(customerId, itemId);
    }

    @PostMapping("/clear")
    @PreAuthorize("hasAuthority('CART_MANAGE')")
    @Operation(
            summary = "Очистить корзину",
            description = "Удаляет все позиции из корзины.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пустая корзина",
                            content = @Content(schema = @Schema(implementation = CartResponse.class)))
            }
    )
    public CartResponse clear(
            @Parameter(description = "ID покупателя") @PathVariable Long customerId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return cartService.clear(customerId);
    }

    private void ensureOwnCustomer(Long customerId, AuthPrincipal principal) {
        if (!principal.getParticipantId().equals(customerId)) {
            throw new BusinessException("Доступ к чужой корзине запрещен");
        }
    }
}
