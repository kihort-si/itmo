package ru.itmo.blps.app.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.blps.app.dto.CreateProductRequest;
import ru.itmo.blps.app.dto.CreatePromoCodeRequest;
import ru.itmo.blps.app.dto.CreateShopRequest;
import ru.itmo.blps.app.dto.UpdateProductRequest;
import ru.itmo.blps.app.dto.UpdateShopRequest;
import ru.itmo.blps.app.models.Product;
import ru.itmo.blps.app.models.PromoCode;
import ru.itmo.blps.app.models.Shop;
import ru.itmo.blps.app.services.CatalogService;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    @PreAuthorize("isAuthenticated()")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Page<Product> getProducts(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(defaultValue = "id,asc") String[] sort,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) Long minPrice,
                                     @RequestParam(required = false) Long maxPrice,
                                     @RequestParam(required = false) Boolean inStock) {
        return catalogService.listProducts(q, minPrice, maxPrice, inStock, pageable(page, size, sort));
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Product createProduct(@RequestBody @Valid CreateProductRequest request) {
        return catalogService.createProduct(request);
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Product updateProduct(@PathVariable Long productId, @RequestBody @Valid CreateProductRequest request) {
        return catalogService.updateProduct(productId, request);
    }

    @PatchMapping("/products/{productId}")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Product patchProduct(@PathVariable Long productId, @RequestBody UpdateProductRequest request) {
        return catalogService.patchProduct(productId, request);
    }

    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public void deleteProduct(@PathVariable Long productId) {
        catalogService.deleteProduct(productId);
    }

    @GetMapping("/shops")
    @PreAuthorize("isAuthenticated()")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Page<Shop> getShops(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "id,asc") String[] sort,
                               @RequestParam(required = false) String q) {
        return catalogService.listShops(q, pageable(page, size, sort));
    }

    @PostMapping("/shops")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Shop createShop(@RequestBody @Valid CreateShopRequest request) {
        return catalogService.createShop(request);
    }

    @PutMapping("/shops/{shopId}")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public Shop updateShop(@PathVariable Long shopId, @RequestBody @Valid CreateShopRequest request) {
        return catalogService.updateShop(shopId, request);
    }

    @DeleteMapping("/shops/{shopId}")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public void deleteShop(@PathVariable Long shopId) {
        catalogService.deleteShop(shopId);
    }

    @PostMapping("/promo-codes")
    @PreAuthorize("hasRole('SHOP_ASSISTANT')")
    @Operation(security = @SecurityRequirement(name = "Bearer"))
    public PromoCode createPromoCode(@RequestBody @Valid CreatePromoCodeRequest request) {
        return catalogService.createPromoCode(request);
    }

    private Pageable pageable(int page, int size, String[] sort) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        String sortField = sort.length > 0 ? sort[0] : "id";
        Sort.Direction direction = sort.length > 1 ? Sort.Direction.fromOptionalString(sort[1]).orElse(Sort.Direction.ASC) : Sort.Direction.ASC;
        return PageRequest.of(Math.max(page, 0), safeSize, Sort.by(direction, sortField));
    }
}
