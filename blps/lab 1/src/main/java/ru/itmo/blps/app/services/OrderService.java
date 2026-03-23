package ru.itmo.blps.app.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps.app.dto.CheckoutRequest;
import ru.itmo.blps.app.dto.OrderItemResponse;
import ru.itmo.blps.app.dto.OrderResponse;
import ru.itmo.blps.app.exceptions.BusinessException;
import ru.itmo.blps.app.exceptions.NotFoundException;
import ru.itmo.blps.app.models.Cart;
import ru.itmo.blps.app.models.CartItem;
import ru.itmo.blps.app.models.Courier;
import ru.itmo.blps.app.models.Delivery;
import ru.itmo.blps.app.models.Order;
import ru.itmo.blps.app.models.OrderItem;
import ru.itmo.blps.app.models.Product;
import ru.itmo.blps.app.models.PromoCode;
import ru.itmo.blps.app.models.Shop;
import ru.itmo.blps.app.models.ShopAssistant;
import ru.itmo.blps.app.models.enums.DeliveryStatus;
import ru.itmo.blps.app.models.enums.FulfillmentMethod;
import ru.itmo.blps.app.models.enums.OrderStatus;
import ru.itmo.blps.app.models.enums.PaymentMethod;
import ru.itmo.blps.app.repositories.CartRepository;
import ru.itmo.blps.app.repositories.CourierRepository;
import ru.itmo.blps.app.repositories.OrderRepository;
import ru.itmo.blps.app.repositories.ProductRepository;
import ru.itmo.blps.app.repositories.PromoCodeRepository;
import ru.itmo.blps.app.repositories.ShopAssistantRepository;
import ru.itmo.blps.app.repositories.ShopRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final ShopRepository shopRepository;
    private final ShopAssistantRepository shopAssistantRepository;
    private final CourierRepository courierRepository;
    private final CartService cartService;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        PromoCodeRepository promoCodeRepository,
                        ShopRepository shopRepository,
                        ShopAssistantRepository shopAssistantRepository,
                        CourierRepository courierRepository,
                        CartService cartService,
                        CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.shopRepository = shopRepository;
        this.shopAssistantRepository = shopAssistantRepository;
        this.courierRepository = courierRepository;
        this.cartService = cartService;
        this.customerService = customerService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse checkout(Long customerId, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCartEntity(customerId);

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Нельзя оформить пустую корзину");
        }

        if (request.fulfillmentMethod() == FulfillmentMethod.PICKUP) {
            if (request.shopId() == null) {
                throw new BusinessException("Для самовывоза необходимо указать ID салона");
            }
        } else if (request.fulfillmentMethod() == FulfillmentMethod.COURIER_DELIVERY) {
            if (request.deliveryAddress() == null || request.deliveryAddress().isBlank()) {
                throw new BusinessException("Для доставки необходимо указать адрес");
            }
        }

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setStatus(OrderStatus.PAID);
        order.setPaid(true);
        order.setDiscountAmount(0L);
        order.setTotalAmount(sumCart(cart));
        order.setFinalAmount(order.getTotalAmount());

        Map<Product, Integer> requiredQtyByProduct = new HashMap<>();

        for (CartItem cartItem : cart.getItems()) {
            requiredQtyByProduct.merge(
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    Integer::sum
            );
        }

        for (Map.Entry<Product, Integer> entry : requiredQtyByProduct.entrySet()) {
            Product product = entry.getKey();
            int requiredQty = entry.getValue();

            if (product.getStock() < requiredQty) {
                throw new BusinessException("Недостаточно товара на складе: " + product.getName());
            }
        }

        for (Map.Entry<Product, Integer> entry : requiredQtyByProduct.entrySet()) {
            Product product = entry.getKey();
            int requiredQty = entry.getValue();

            product.setStock(product.getStock() - requiredQty);
        }

        productRepository.saveAll(requiredQtyByProduct.keySet());

        cart.getItems().clear();
        cartRepository.save(cart);

        if (hasText(request.promoCode())) {
            try{
                applyPromoInternal(order, request.promoCode());
            } catch (Exception e) {
                order.setPromoCode(null);
            }
        }

        selectFulfillmentInternal(order, request.fulfillmentMethod());

        selectPaymentInternal(order, request.paymentMethod());

        if (request.fulfillmentMethod() == FulfillmentMethod.PICKUP) {
            selectPickupShopInternal(order, request.shopId());
        } else if (request.fulfillmentMethod() == FulfillmentMethod.COURIER_DELIVERY) {
            provideDeliveryAddressInternal(order, request.deliveryAddress());
            Courier courier = courierRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new NotFoundException("Нет доступных курьеров"));
            Delivery delivery = order.getDelivery();
            if (delivery == null) {
                delivery = new Delivery();
                delivery.setOrder(order);
                order.setDelivery(delivery);
            }
            delivery.setCourier(courier);
            delivery.setStatus(DeliveryStatus.ORDER_RECEIVED_BY_COURIER);
            order.setStatus(OrderStatus.COURIER_ASSIGNED);
        }

        if (request.paymentMethod() == PaymentMethod.ONLINE) {
            payOnlineInternal(order);
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse markReadyForPickupByAssistant(Long orderId, Long assistantId) {
        Order order = getOrder(orderId);
        ensureShopAssistantCanProcess(order, assistantId);

        if (order.getFulfillmentMethod() != FulfillmentMethod.PICKUP) {
            throw new BusinessException("Этот заказ не для самовывоза");
        }

        if (order.getStatus() != OrderStatus.PICKUP_SHOP_SELECTED) {
            throw new BusinessException("Заказ должен быть в статусе PICKUP_SHOP_SELECTED");
        }

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse markReadyForDeliveryByAssistant(Long orderId, Long assistantId) {
        Order order = getOrder(orderId);
        ensureShopAssistantCanProcess(order, assistantId);

        if (order.getFulfillmentMethod() != FulfillmentMethod.COURIER_DELIVERY) {
            throw new BusinessException("Этот заказ не для доставки курьером");
        }

        if (order.getStatus() != OrderStatus.DELIVERY_ADDRESS_PROVIDED) {
            throw new BusinessException("Заказ должен быть в статусе DELIVERY_ADDRESS_PROVIDED");
        }

        order.setStatus(OrderStatus.COURIER_ASSIGNED);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse getById(Long orderId, Long participantId, Collection<? extends GrantedAuthority> authorities) {
        Order order = getOrder(orderId);

        if (hasAuthority(authorities, "ROLE_CUSTOMER")) {
            ensureCustomerOwnsOrder(order, participantId);
        } else if (hasAuthority(authorities, "ROLE_SHOP_ASSISTANT")) {
            ensureShopAssistantCanProcess(order, participantId);
        } else if (hasAuthority(authorities, "ROLE_COURIER")) {
            ensureCourierAssigned(order, participantId);
        }

        return toResponse(order);
    }

    public Page<OrderResponse> getByCustomer(Long customerId, OrderStatus status, Pageable pageable) {
        customerService.getById(customerId);

        Page<Order> page = status == null
                ? orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                : orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status, pageable);

        return page.map(this::toResponse);
    }

    private void ensureCustomerOwnsOrder(Order order, Long customerId) {
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException("Доступ к чужому заказу запрещен");
        }
    }

    private void applyPromoInternal(Order order, String rawCode) {
        ensureStatus(order, OrderStatus.CHECKED_OUT);

        PromoCode promoCode = promoCodeRepository.findById(rawCode.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Промокод не найден"));

        if (!promoCode.isActive()) {
            throw new BusinessException("Промокод неактивен");
        }

        long discount = Math.round(order.getTotalAmount() * promoCode.getDiscountPercent() / 100.0);
        order.setPromoCode(promoCode.getCode());
        order.setDiscountAmount(discount);
        order.setFinalAmount(order.getTotalAmount() - discount);
    }

    private void selectFulfillmentInternal(Order order, FulfillmentMethod fulfillmentMethod) {
        ensureStatus(order, OrderStatus.CHECKED_OUT);
        order.setFulfillmentMethod(fulfillmentMethod);
        order.setDeliveryEta(fulfillmentMethod == FulfillmentMethod.PICKUP ? "1-2 дня" : "2-4 дня");
        order.setStatus(OrderStatus.FULFILLMENT_SELECTED);
    }

    private void selectPaymentInternal(Order order, PaymentMethod paymentMethod) {
        ensureStatus(order, OrderStatus.FULFILLMENT_SELECTED);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.WAITING_PAYMENT);
    }

    private void payOnlineInternal(Order order) {
        ensureStatus(order, OrderStatus.WAITING_PAYMENT);
        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BusinessException("Онлайн-оплата выбрана не была");
        }
        order.setPaid(true);
        order.setStatus(OrderStatus.PAID);
    }

    private void selectPickupShopInternal(Order order, Long shopId) {
        ensureFulfillment(order, FulfillmentMethod.PICKUP);
        ensureStageForNextStep(order, OrderStatus.WAITING_PAYMENT, OrderStatus.PAID);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Салон связи не найден: id=" + shopId));
        order.setShop(shop);
        order.setStatus(OrderStatus.PICKUP_SHOP_SELECTED);
    }

    private void provideDeliveryAddressInternal(Order order, String address) {
        ensureFulfillment(order, FulfillmentMethod.COURIER_DELIVERY);
        ensureStageForNextStep(order, OrderStatus.WAITING_PAYMENT, OrderStatus.PAID);
        order.setDeliveryAddress(address.trim());
        order.setStatus(OrderStatus.DELIVERY_ADDRESS_PROVIDED);
    }

    private void finalizeOrder(Order order) {
        if (!order.isPaid()) {
            if (order.getPaymentMethod() != PaymentMethod.CASH_ON_RECEIPT) {
                throw new BusinessException("Заказ не оплачен");
            }
            order.setPaid(true);
        }
        order.setStatus(OrderStatus.COMPLETED);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден: id=" + orderId));
    }

    private void ensureStatus(Order order, OrderStatus status) {
        if (order.getStatus() != status) {
            throw new BusinessException(
                    "Неверный шаг процесса. Текущий статус: " + order.getStatus() + ", ожидается: " + status
            );
        }
    }

    private void ensureStageForNextStep(Order order, OrderStatus option1, OrderStatus option2) {
        if (order.getStatus() != option1 && order.getStatus() != option2) {
            throw new BusinessException("Неверная стадия заказа для этого действия");
        }
    }

    private void ensureFulfillment(Order order, FulfillmentMethod expected) {
        if (order.getFulfillmentMethod() != expected) {
            throw new BusinessException("Неверный способ получения для этого действия");
        }
    }

    private void ensureShopAssistantCanProcess(Order order, Long assistantId) {
        ShopAssistant assistant = shopAssistantRepository.findById(assistantId)
                .orElseThrow(() -> new NotFoundException("Сотрудник салона не найден: id=" + assistantId));

        if (order.getShop() == null || assistant.getShop() == null ||
                !order.getShop().getId().equals(assistant.getShop().getId())) {
            throw new BusinessException("Сотрудник не может работать с этим заказом");
        }
    }

    private void ensureCourierAssigned(Order order, Long courierId) {
        if (order.getDelivery() == null || order.getDelivery().getCourier() == null) {
            throw new BusinessException("Для заказа не назначен курьер");
        }
        if (!order.getDelivery().getCourier().getId().equals(courierId)) {
            throw new BusinessException("Курьер не может работать с чужим заказом");
        }
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream().anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private long sumCart(Cart cart) {
        return cart.getItems().stream()
                .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }


    @Transactional
    public OrderResponse courierDeliveredOrder(Long orderId, Long courierId) {
        Order order = getOrder(orderId);
        ensureCourierAssigned(order, courierId);

        if (order.getStatus() == OrderStatus.COURIER_ASSIGNED) {
            order.getDelivery().setStatus(DeliveryStatus.PICKED_UP_FROM_STORE);
            order.setStatus(OrderStatus.COURIER_PICKED_UP);
        }

        if (order.getStatus() != OrderStatus.COURIER_PICKED_UP) {
            throw new BusinessException("Курьер должен сначала забрать заказ");
        }

        order.getDelivery().setStatus(DeliveryStatus.DELIVERED_TO_CUSTOMER);
        order.setStatus(OrderStatus.COURIER_DELIVERED);
        finalizeOrder(order);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse assistantCompletePickup(Long orderId, Long assistantId) {
        Order order = getOrder(orderId);
        ensureShopAssistantCanProcess(order, assistantId);

        if (order.getFulfillmentMethod() != FulfillmentMethod.PICKUP) {
            throw new BusinessException("Этот заказ не для самовывоза");
        }

        if (order.getStatus() != OrderStatus.READY_NOTIFICATION_SENT) {
            throw new BusinessException("Заказ должен быть в статусе READY_NOTIFICATION_SENT для завершения самовывоза");
        }

        finalizeOrder(order);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getFulfillmentMethod(),
                order.getPaymentMethod(),
                order.isPaid(),
                order.getPromoCode(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getFinalAmount(),
                order.getDeliveryAddress(),
                order.getShop() == null ? null : order.getShop().getId(),
                order.getDeliveryEta(),
                order.getDelivery() == null || order.getDelivery().getCourier() == null
                        ? null
                        : order.getDelivery().getCourier().getName(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getUnitPrice() * item.getQuantity()
                        ))
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
