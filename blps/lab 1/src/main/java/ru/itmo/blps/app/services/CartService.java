package ru.itmo.blps.app.services;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps.app.dto.CartItemResponse;
import ru.itmo.blps.app.dto.CartResponse;
import ru.itmo.blps.app.exceptions.BusinessException;
import ru.itmo.blps.app.exceptions.NotFoundException;
import ru.itmo.blps.app.models.Cart;
import ru.itmo.blps.app.models.CartItem;
import ru.itmo.blps.app.models.Customer;
import ru.itmo.blps.app.models.Product;
import ru.itmo.blps.app.repositories.CartRepository;
import ru.itmo.blps.app.repositories.ProductRepository;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       CustomerService customerService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.customerService = customerService;
    }

    @Transactional
    public CartResponse getOrCreateCart(Long customerId) {
        return toResponse(getOrCreateCartEntity(customerId));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CartResponse addItem(Long customerId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCartEntity(customerId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар не найден: id=" + productId));

        if (product.getStock() < quantity) {
            throw new BusinessException("Недостаточно товара на складе");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            cart.getItems().add(item);
        } else {
            int newQuantity = item.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new BusinessException("Недостаточно товара на складе");
            }
            item.setQuantity(newQuantity);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CartResponse updateItem(Long customerId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCartEntity(customerId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Позиция корзины не найдена: id=" + itemId));

        if (item.getProduct().getStock() < quantity) {
            throw new BusinessException("Недостаточно товара на складе");
        }

        item.setQuantity(quantity);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CartResponse removeItem(Long customerId, Long itemId) {
        Cart cart = getOrCreateCartEntity(customerId);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new NotFoundException("Позиция корзины не найдена: id=" + itemId);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CartResponse clear(Long customerId) {
        Cart cart = getOrCreateCartEntity(customerId);
        cart.getItems().clear();
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public Cart getOrCreateCartEntity(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Customer customer = customerService.getById(customerId);
                    Cart cart = new Cart();
                    cart.setCustomer(customer);
                    cart.setItems(new ArrayList<>());
                    return cartRepository.save(cart);
                });
    }

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice() * item.getQuantity()
                ))
                .toList();

        long total = items.stream().mapToLong(CartItemResponse::lineAmount).sum();

        return new CartResponse(cart.getId(), cart.getCustomer().getId(), items, total);
    }
}
