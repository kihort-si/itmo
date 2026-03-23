package ru.itmo.blps.app.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps.app.dto.CreateProductRequest;
import ru.itmo.blps.app.dto.CreatePromoCodeRequest;
import ru.itmo.blps.app.dto.CreateShopRequest;
import ru.itmo.blps.app.dto.UpdateProductRequest;
import ru.itmo.blps.app.dto.UpdateShopRequest;
import ru.itmo.blps.app.exceptions.NotFoundException;
import ru.itmo.blps.app.models.Product;
import ru.itmo.blps.app.models.PromoCode;
import ru.itmo.blps.app.models.Shop;
import ru.itmo.blps.app.repositories.ProductRepository;
import ru.itmo.blps.app.repositories.PromoCodeRepository;
import ru.itmo.blps.app.repositories.ShopRepository;

@Service
public class CatalogService {
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final PromoCodeRepository promoCodeRepository;

    public CatalogService(ProductRepository productRepository,
                          ShopRepository shopRepository,
                          PromoCodeRepository promoCodeRepository) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.promoCodeRepository = promoCodeRepository;
    }

    public Page<Product> listProducts(String query,
                                      Long minPrice,
                                      Long maxPrice,
                                      Boolean inStock,
                                      Pageable pageable) {
        Specification<Product> specification = (root, cq, cb) -> cb.conjunction();

        if (query != null && !query.isBlank()) {
            String normalizedQuery = "%" + query.trim().toLowerCase() + "%";
            specification = specification.and((root, cq, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), normalizedQuery),
                            cb.like(cb.lower(root.get("description")), normalizedQuery)
                    )
            );
        }

        if (minPrice != null) {
            specification = specification.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            specification = specification.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (Boolean.TRUE.equals(inStock)) {
            specification = specification.and((root, cq, cb) -> cb.greaterThan(root.get("stock"), 0));
        }

        return productRepository.findAll(specification, pageable);
    }

    public Page<Shop> listShops(String query, Pageable pageable) {
        Specification<Shop> specification = (root, cq, cb) -> cb.conjunction();

        if (query != null && !query.isBlank()) {
            String normalizedQuery = "%" + query.trim().toLowerCase() + "%";
            specification = specification.and((root, cq, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), normalizedQuery),
                            cb.like(cb.lower(root.get("address")), normalizedQuery)
                    )
            );
        }

        return shopRepository.findAll(specification, pageable);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Product createProduct(CreateProductRequest request) {
        Product product = new Product(
                null,
                request.name().trim(),
                request.description().trim(),
                request.price(),
                request.stock()
        );
        return productRepository.save(product);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Product updateProduct(Long productId, CreateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар не найден: id=" + productId));
        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        return productRepository.save(product);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Product patchProduct(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар не найден: id=" + productId));
        if (request.name() != null && !request.name().isBlank()) {
            product.setName(request.name().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            product.setDescription(request.description().trim());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stock() != null) {
            product.setStock(request.stock());
        }
        return productRepository.save(product);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Товар не найден: id=" + productId);
        }
        productRepository.deleteById(productId);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Shop createShop(CreateShopRequest request) {
        Shop shop = new Shop(null, request.name().trim(), request.address().trim());
        return shopRepository.save(shop);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Shop updateShop(Long shopId, CreateShopRequest request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Салон связи не найден: id=" + shopId));
        shop.setName(request.name().trim());
        shop.setAddress(request.address().trim());
        return shopRepository.save(shop);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteShop(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new NotFoundException("Салон связи не найден: id=" + shopId);
        }
        shopRepository.deleteById(shopId);
    }

    @Transactional
    public PromoCode createPromoCode(CreatePromoCodeRequest request) {
        PromoCode promoCode = new PromoCode(
                request.code().trim().toUpperCase(),
                request.discountPercent(),
                request.active()
        );
        return promoCodeRepository.save(promoCode);
    }
}
