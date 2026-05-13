package ru.itmo.blps.app.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.blps.app.dto.CheckoutRequest;
import ru.itmo.blps.app.dto.OrderResponse;
import ru.itmo.blps.app.exceptions.BusinessException;
import ru.itmo.blps.app.models.enums.OrderStatus;
import ru.itmo.blps.app.security.AuthPrincipal;
import ru.itmo.blps.app.services.OrderService;

@RestController
@RequestMapping("/api")
@Tag(name = "Заказы", description = "Оформление и управление заказами. Разные операции доступны покупателям, сотрудникам салонов и курьерам.")
@SecurityRequirement(name = "basicAuth")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/customers/{customerId}/orders/checkout")
    @PreAuthorize("hasAuthority('ORDER_CHECKOUT')")
    @Operation(
            summary = "Оформить заказ",
            description = """
                    Создаёт заказ из текущей корзины покупателя. В одном запросе выполняется:
                    1. Резервирование товаров (уменьшение остатков)
                    2. Применение промокода (если указан)
                    3. Выбор способа получения (PICKUP / COURIER_DELIVERY)
                    4. Выбор способа оплаты (ONLINE / CASH_ON_RECEIPT)
                    5. Онлайн-оплата (если выбрана)
                    6. Указание салона для самовывоза или адреса доставки

                    Для PICKUP: обязателен shopId. Для COURIER_DELIVERY: обязателен deliveryAddress.
                    Операция выполняется в транзакции с изоляцией SERIALIZABLE.
                    Доступно только роли CUSTOMER для своего customerId.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Созданный заказ",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Пустая корзина, нет товара на складе или ошибка валидации",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Нельзя оформить пустую корзину\"}"))),
                    @ApiResponse(responseCode = "403", description = "Попытка оформить заказ за другого покупателя",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ к чужим заказам запрещен\"}"))),
                    @ApiResponse(responseCode = "404", description = "Указанный салон не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Салон связи не найден: id=5\"}")))
            }
    )
    public OrderResponse checkout(
            @Parameter(description = "ID покупателя") @PathVariable Long customerId,
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return orderService.checkout(customerId, request);
    }

    @GetMapping("/customers/{customerId}/orders")
    @PreAuthorize("hasAuthority('ORDER_READ_OWN')")
    @Operation(
            summary = "Заказы покупателя",
            description = "Возвращает постраничный список заказов покупателя, отсортированных по дате (новые первыми). " +
                    "Можно фильтровать по статусу. Доступно только роли CUSTOMER для своего customerId.",
            parameters = {
                    @Parameter(name = "status", description = "Фильтр по статусу заказа"),
                    @Parameter(name = "page", description = "Номер страницы (с 0)"),
                    @Parameter(name = "size", description = "Размер страницы (1–100, по умолчанию 20)")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страница заказов"),
                    @ApiResponse(responseCode = "403", description = "Попытка просмотра чужих заказов",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ к чужим заказам запрещен\"}")))
            }
    )
    public Page<OrderResponse> getCustomerOrders(
            @Parameter(description = "ID покупателя") @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return orderService.getByCustomer(customerId, status, pageable(page, size, sort));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyAuthority('ORDER_READ_OWN','ORDER_READ_ASSIGNED')")
    @Operation(
            summary = "Получить заказ по ID",
            description = """
                    Возвращает детали заказа. Доступ зависит от роли:
                    - CUSTOMER: только свои заказы
                    - SHOP_ASSISTANT: только заказы своего салона
                    - COURIER: только заказы, в которых он назначен курьером
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Детали заказа",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Попытка доступа к чужому заказу",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ к чужому заказу запрещен\"}"))),
                    @ApiResponse(responseCode = "404", description = "Заказ не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ не найден: id=42\"}")))
            }
    )
    public OrderResponse getOrder(
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.getById(orderId, principal.getParticipantId(), principal.getAuthorities());
    }

    @PostMapping("/orders/{orderId}/ready-for-pickup")
    @PreAuthorize("hasAuthority('ORDER_PROCESS_PICKUP')")
    @Operation(
            summary = "Пометить заказ готовым к выдаче",
            description = """
                    Сотрудник салона переводит заказ в статус READY_FOR_PICKUP.
                    Заказ должен быть в статусе PICKUP_SHOP_SELECTED и принадлежать салону сотрудника.
                    Доступно только роли SHOP_ASSISTANT.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённый заказ",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Неверный статус заказа или сотрудник из другого салона",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ должен быть в статусе PICKUP_SHOP_SELECTED\"}")))
            }
    )
    public OrderResponse markReadyForPickup(
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.markReadyForPickupByAssistant(orderId, principal.getParticipantId());
    }

    @PostMapping("/orders/{orderId}/assistant/complete-pickup")
    @PreAuthorize("hasAuthority('ORDER_PROCESS_PICKUP')")
    @Operation(
            summary = "Завершить самовывоз",
            description = """
                    Сотрудник салона подтверждает, что покупатель забрал заказ.
                    Заказ переходит в статус COMPLETED. При оплате наличными — помечается как оплаченный.
                    Заказ должен быть в статусе READY_FOR_PICKUP.
                    Доступно только роли SHOP_ASSISTANT.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Завершённый заказ",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Неверный статус заказа",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ должен быть в статусе READY_FOR_PICKUP\"}")))
            }
    )
    public OrderResponse assistantCompletePickup(
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.assistantCompletePickup(orderId, principal.getParticipantId());
    }

    @PostMapping("/orders/{orderId}/ready-for-delivery")
    @PreAuthorize("hasAuthority('ORDER_PROCESS_DELIVERY')")
    @Operation(
            summary = "Передать заказ курьеру",
            description = """
                    Сотрудник салона назначает курьера и передаёт заказ на доставку.
                    Заказ переходит в статус COURIER_ASSIGNED.
                    Курьер выбирается автоматически из доступных.
                    Заказ должен быть в статусе DELIVERY_ADDRESS_PROVIDED.
                    Доступно только роли SHOP_ASSISTANT.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Заказ передан курьеру",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Неверный статус или способ получения",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ должен быть в статусе DELIVERY_ADDRESS_PROVIDED\"}"))),
                    @ApiResponse(responseCode = "404", description = "Нет доступных курьеров",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Нет доступных курьеров\"}")))
            }
    )
    public OrderResponse markReadyForDelivery(
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.markReadyForDeliveryByAssistant(orderId, principal.getParticipantId());
    }

    @PostMapping("/orders/{orderId}/courier/delivered")
    @PreAuthorize("hasAuthority('ORDER_PROCESS_DELIVERY')")
    @Operation(
            summary = "Подтвердить доставку",
            description = """
                    Курьер подтверждает доставку заказа покупателю.
                    При первом вызове (статус COURIER_ASSIGNED): заказ переходит в COURIER_PICKED_UP (курьер забрал из салона).
                    При втором вызове (статус COURIER_PICKED_UP): заказ завершается (COMPLETED).
                    При оплате наличными — помечается как оплаченный.
                    Доступно только роли COURIER для назначенного курьера.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённый заказ",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Курьер не назначен или неверный статус",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Курьер должен сначала забрать заказ\"}")))
            }
    )
    public OrderResponse courierDeliveredOrder(
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.courierDeliveredOrder(orderId, principal.getParticipantId());
    }

    private void ensureOwnCustomer(Long customerId, AuthPrincipal principal) {
        if (!principal.getParticipantId().equals(customerId)) {
            throw new BusinessException("Доступ к чужим заказам запрещен");
        }
    }

    private Pageable pageable(int page, int size, String[] sort) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        String sortField = sort.length > 0 ? sort[0] : "createdAt";
        Sort.Direction direction = sort.length > 1 ? Sort.Direction.fromOptionalString(sort[1]).orElse(Sort.Direction.DESC) : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), safeSize, Sort.by(direction, sortField));
    }
}
