package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.dto.ProductCatalogRequestDto;
import ru.itmo.se.is.cw.dto.ProductCatalogResponseDto;
import ru.itmo.se.is.cw.dto.filter.ProductCatalogFilter;
import ru.itmo.se.is.cw.service.CatalogService;

import java.net.URI;


@RestController
@RequestMapping("/catalog")
@Tag(name = "Catalog", description = "Операции с каталогом товаров")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    @Operation(
            summary = "Список товаров каталога",
            description = "Возвращает пагинированный список товаров."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пагинированный список товаров"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный параметр запроса",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<Page<ProductCatalogResponseDto>> getProducts(
            @ParameterObject @ModelAttribute ProductCatalogFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ProductCatalogResponseDto> products = catalogService.getProducts(pageable, filter);
        return ResponseEntity.ok(products);
    }


    @PostMapping
    @Operation(
            summary = "Создать товар каталога (админ)",
            description = "Создает новый товар каталога. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Товар создан",
                    content = @Content(
                            schema = @Schema(implementation = ProductCatalogResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_catalog.write')")
    public ResponseEntity<ProductCatalogResponseDto> createProduct(
            @RequestBody ProductCatalogRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        ProductCatalogResponseDto item = catalogService.createProduct(request);
        URI location = uriBuilder
                .path("/catalog/{id}")
                .buildAndExpand(item.getId())
                .toUri();
        return ResponseEntity.created(location).body(item);
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Детали товара",
            description = "Возвращает полную информацию о товаре каталога по его идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар найден",
                    content = @Content(
                            schema = @Schema(implementation = ProductCatalogResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<ProductCatalogResponseDto> getProductById(
            @PathVariable @Parameter(description = "Идентификатор товара", required = true) Long id
    ) {
        ProductCatalogResponseDto item = catalogService.getProductById(id);
        return ResponseEntity.ok(item);
    }


    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить товар каталога (админ)",
            description = "Обновляет существующий товар каталога по ID. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар обновлён",
                    content = @Content(
                            schema = @Schema(implementation = ProductCatalogResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_catalog.write')")
    public ResponseEntity<ProductCatalogResponseDto> updateProduct(
            @PathVariable @Parameter(description = "Идентификатор товара", required = true) Long id,
            @RequestBody ProductCatalogRequestDto request
    ) {
        ProductCatalogResponseDto item = catalogService.updateProduct(id, request);
        return ResponseEntity.ok(item);
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить товар каталога (админ)",
            description = "Удаляет товар каталога по ID. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Товар удалён",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_catalog.write')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable @Parameter(description = "Идентификатор товара", required = true) Long id
    ) {
        catalogService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
