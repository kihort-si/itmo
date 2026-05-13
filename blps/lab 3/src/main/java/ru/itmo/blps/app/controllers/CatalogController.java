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
@Tag(name = "Каталог", description = "Товары, салоны связи и промокоды")
@SecurityRequirement(name = "basicAuth")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('CATALOG_READ')")
    @Operation(
            summary = "Список товаров",
            description = "Возвращает постраничный список товаров с фильтрацией. Доступно покупателям и сотрудникам салонов.",
            parameters = {
                    @Parameter(name = "q", description = "Поиск по названию и описанию (case-insensitive)"),
                    @Parameter(name = "minPrice", description = "Минимальная цена (в копейках)"),
                    @Parameter(name = "maxPrice", description = "Максимальная цена (в копейках)"),
                    @Parameter(name = "inStock", description = "Только товары в наличии"),
                    @Parameter(name = "page", description = "Номер страницы (с 0)"),
                    @Parameter(name = "size", description = "Размер страницы (1–100, по умолчанию 20)"),
                    @Parameter(name = "sort", description = "Сортировка, например: id,asc или price,desc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страница товаров"),
                    @ApiResponse(responseCode = "401", description = "Не аутентифицирован",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Требуется авторизация\"}")))
            }
    )
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
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @Operation(
            summary = "Создать товар",
            description = "Создаёт новый товар в каталоге. Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Созданный товар",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(schema = @Schema(example = "{\"error\": \"name: must not be blank\"}"))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ запрещен\"}")))
            }
    )
    public Product createProduct(@RequestBody @Valid CreateProductRequest request) {
        return catalogService.createProduct(request);
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @Operation(
            summary = "Обновить товар (полная замена)",
            description = "Полностью заменяет все поля товара. Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённый товар",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "404", description = "Товар не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Товар не найден: id=1\"}")))
            }
    )
    public Product updateProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @RequestBody @Valid CreateProductRequest request) {
        return catalogService.updateProduct(productId, request);
    }

    @PatchMapping("/products/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @Operation(
            summary = "Обновить товар (частичное обновление)",
            description = "Обновляет только переданные поля товара. Пустые/null поля игнорируются. Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённый товар",
                            content = @Content(schema = @Schema(implementation = Product.class))),
                    @ApiResponse(responseCode = "404", description = "Товар не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Товар не найден: id=1\"}")))
            }
    )
    public Product patchProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request) {
        return catalogService.patchProduct(productId, request);
    }

    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @Operation(
            summary = "Удалить товар",
            description = "Удаляет товар из каталога. Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Товар удалён"),
                    @ApiResponse(responseCode = "404", description = "Товар не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Товар не найден: id=1\"}")))
            }
    )
    public void deleteProduct(@Parameter(description = "ID товара") @PathVariable Long productId) {
        catalogService.deleteProduct(productId);
    }

    @GetMapping("/shops")
    @PreAuthorize("hasAuthority('CATALOG_READ')")
    @Operation(
            summary = "Список салонов связи",
            description = "Возвращает постраничный список салонов с поиском по названию и адресу.",
            parameters = {
                    @Parameter(name = "q", description = "Поиск по названию и адресу (case-insensitive)"),
                    @Parameter(name = "page", description = "Номер страницы (с 0)"),
                    @Parameter(name = "size", description = "Размер страницы (1–100, по умолчанию 20)"),
                    @Parameter(name = "sort", description = "Сортировка, например: name,asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Страница салонов")
            }
    )
    public Page<Shop> getShops(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "id,asc") String[] sort,
                               @RequestParam(required = false) String q) {
        return catalogService.listShops(q, pageable(page, size, sort));
    }

    @PostMapping("/shops")
    @PreAuthorize("hasAuthority('SHOP_MANAGE')")
    @Operation(
            summary = "Создать салон связи",
            description = "Создаёт новый салон. Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Созданный салон",
                            content = @Content(schema = @Schema(implementation = Shop.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(schema = @Schema(example = "{\"error\": \"address: must not be blank\"}")))
            }
    )
    public Shop createShop(@RequestBody @Valid CreateShopRequest request) {
        return catalogService.createShop(request);
    }

    @PutMapping("/shops/{shopId}")
    @PreAuthorize("hasAuthority('SHOP_MANAGE')")
    @Operation(
            summary = "Обновить салон связи",
            description = "Полностью заменяет данные салона. Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Обновлённый салон",
                            content = @Content(schema = @Schema(implementation = Shop.class))),
                    @ApiResponse(responseCode = "404", description = "Салон не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Салон связи не найден: id=2\"}")))
            }
    )
    public Shop updateShop(
            @Parameter(description = "ID салона") @PathVariable Long shopId,
            @RequestBody @Valid CreateShopRequest request) {
        return catalogService.updateShop(shopId, request);
    }

    @DeleteMapping("/shops/{shopId}")
    @PreAuthorize("hasAuthority('SHOP_MANAGE')")
    @Operation(
            summary = "Удалить салон связи",
            description = "Удаляет салон. Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Салон удалён"),
                    @ApiResponse(responseCode = "404", description = "Салон не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Салон связи не найден: id=2\"}")))
            }
    )
    public void deleteShop(@Parameter(description = "ID салона") @PathVariable Long shopId) {
        catalogService.deleteShop(shopId);
    }

    @PostMapping("/promo-codes")
    @PreAuthorize("hasAuthority('PROMO_MANAGE')")
    @Operation(
            summary = "Создать промокод",
            description = "Создаёт промокод со скидкой в процентах. Код автоматически приводится к верхнему регистру. " +
                    "Доступно только сотрудникам салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Созданный промокод",
                            content = @Content(schema = @Schema(implementation = PromoCode.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или код уже существует",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Операция нарушает целостность данных\"}")))
            }
    )
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