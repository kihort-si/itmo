package ru.itmo.blps.app.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/customers/{customerId}/orders/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public OrderResponse checkout(@PathVariable Long customerId,
                                  @Valid @RequestBody CheckoutRequest request,
                                  @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return orderService.checkout(customerId, request);
    }

    @GetMapping("/customers/{customerId}/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Page<OrderResponse> getCustomerOrders(@PathVariable Long customerId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(defaultValue = "createdAt,desc") String[] sort,
                                                 @RequestParam(required = false) OrderStatus status,
                                                 @AuthenticationPrincipal AuthPrincipal principal) {
        ensureOwnCustomer(customerId, principal);
        return orderService.getByCustomer(customerId, status, pageable(page, size, sort));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public OrderResponse getOrder(@PathVariable Long orderId, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.getById(orderId, principal.getParticipantId(), principal.getAuthorities());
    }

    @PostMapping("/orders/{orderId}/ready-for-pickup")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public OrderResponse markReadyForPickup(@PathVariable Long orderId, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.markReadyForPickupByAssistant(orderId, principal.getParticipantId());
    }

    @PostMapping("/orders/{orderId}/ready-for-delivery")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public OrderResponse markReadyForDelivery(@PathVariable Long orderId, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.markReadyForDeliveryByAssistant(orderId, principal.getParticipantId());
    }

    @PostMapping("/orders/{orderId}/assistant/complete-pickup")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public OrderResponse assistantCompletePickup(@PathVariable Long orderId, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.assistantCompletePickup(orderId, principal.getParticipantId());
    }

    @PostMapping("/orders/{orderId}/courier/delivered")
    @PreAuthorize("hasRole('COURIER')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public OrderResponse courierDeliveredOrder(@PathVariable Long orderId, @AuthenticationPrincipal AuthPrincipal principal) {
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
