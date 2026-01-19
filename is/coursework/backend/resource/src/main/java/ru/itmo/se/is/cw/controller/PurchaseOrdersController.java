package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import ru.itmo.se.is.cw.dto.*;
import ru.itmo.se.is.cw.dto.filter.PurchaseOrderFilter;
import ru.itmo.se.is.cw.service.PurchaseOrdersService;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/purchase-orders")
@Tag(name = "PurchaseOrders", description = "Операции с заявками на закупку")
@RequiredArgsConstructor
public class PurchaseOrdersController {

    private final PurchaseOrdersService purchaseOrdersService;

    @PostMapping
    @Operation(
            summary = "Создание заявки на закупку",
            description = "Создает новую заявку на закупку материалов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заявка создана",
                    content = @Content(
                            schema = @Schema(implementation = PurchaseOrderResponseDto.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_po.write')")
    public ResponseEntity<PurchaseOrderResponseDto> createPurchaseOrder(
            @RequestBody PurchaseOrderRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        PurchaseOrderResponseDto purchaseOrder = purchaseOrdersService.createPurchaseOrder(request);
        URI location = uriBuilder
                .path("/{id}")
                .buildAndExpand(purchaseOrder.getId())
                .toUri();
        return ResponseEntity.created(location).body(purchaseOrder);
    }


    @GetMapping
    @Operation(
            summary = "Список заявок на закупку",
            description = "Возвращает список всех заявок на закупку."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Заявки на закупку",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PurchaseOrderResponseDto.class))
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_po.read')")
    public ResponseEntity<Page<PurchaseOrderResponseDto>> getPurchaseOrders(
            @ParameterObject @ModelAttribute PurchaseOrderFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<PurchaseOrderResponseDto> orders = purchaseOrdersService.getPurchaseOrders(pageable, filter);
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Детали заявки на закупку",
            description = "Возвращает подробную информацию о заявке на закупку по её идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Заявка найдена",
                    content = @Content(
                            schema = @Schema(implementation = PurchaseOrderResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_po.read')")
    public ResponseEntity<PurchaseOrderResponseDto> getPurchaseOrderById(
            @PathVariable @Parameter(description = "Идентификатор заявки", required = true) Long id
    ) {
        PurchaseOrderResponseDto purchaseOrder = purchaseOrdersService.getPurchaseOrderById(id);
        return ResponseEntity.ok(purchaseOrder);
    }


    @PostMapping("/{id}/materials")
    @Operation(
            summary = "Добавить или обновить материалы в заявке",
            description = "Обновляет список материалов, включённых в заявку на закупку."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Материалы обновлены",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_po.write')")
    public ResponseEntity<PurchaseOrderResponseDto> updateMaterialsInPurchaseOrder(
            @PathVariable @Parameter(description = "Идентификатор заявки", required = true) Long id,

            @RequestBody List<PurchaseOrderMaterialDto> materials
    ) {
        PurchaseOrderResponseDto purchaseOrder = purchaseOrdersService.updateMaterialsInPurchaseOrder(id, materials);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PostMapping("/{id}/receipt")
    @Operation(
            summary = "Зарегистрировать приход материалов по заявке",
            description = "Создает запись о приходе материалов по утверждённой заявке."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Приход зарегистрирован",
                    content = @Content(
                            schema = @Schema(implementation = PurchaseOrderReceiptResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заявка не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_po.receive')")
    public ResponseEntity<PurchaseOrderReceiptResponseDto> registerReceipt(
            @PathVariable @Parameter(description = "Идентификатор заявки", required = true) Long id,
            @RequestBody PurchaseOrderReceiptRequest request
    ) {
        PurchaseOrderReceiptResponseDto receipt = purchaseOrdersService.registerReceipt(id, request);
        return ResponseEntity.ok(receipt);
    }
}
