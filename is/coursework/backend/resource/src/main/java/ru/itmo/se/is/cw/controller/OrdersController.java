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
import ru.itmo.se.is.cw.dto.filter.ClientOrderFilter;
import ru.itmo.se.is.cw.service.ConversationsService;
import ru.itmo.se.is.cw.service.MaterialsService;
import ru.itmo.se.is.cw.service.OrdersService;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Операции с заказами")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;
    private final MaterialsService materialsService;
    private final ConversationsService conversationsService;

    @PostMapping
    @Operation(
            summary = "Создание заказа на основе клиентской заявки",
            description = "Создает новый заказ по переданной заявке клиента."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заказ успешно создан",
                    content = @Content(
                            schema = @Schema(implementation = ClientOrderResponseDto.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.write')")
    public ResponseEntity<ClientOrderResponseDto> createOrder(
            @RequestBody CreateOrderRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        ClientOrderResponseDto created = ordersService.createOrder(request);
        URI location = uriBuilder
                .path("/orders/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }


    @GetMapping
    @Operation(
            summary = "Список заказов",
            description = "Возвращает заказы"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список заказов",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ClientOrderResponseDto.class))
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.read')")
    public ResponseEntity<Page<ClientOrderResponseDto>> getOrders(
            @ParameterObject @ModelAttribute ClientOrderFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ordersService.getOrders(pageable, filter));
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Детали заказа",
            description = "Возвращает подробную информацию о заказе по идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Заказ найден",
                    content = @Content(
                            schema = @Schema(implementation = ClientOrderResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.read')")
    public ResponseEntity<ClientOrderResponseDto> getOrderById(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id
    ) {
        return ResponseEntity.ok(ordersService.getOrderById(id));
    }

    @GetMapping("/{id}/conversation")
    @Operation(
            summary = "Получить диалог по заказу",
            description = "Возвращает диалог, связанный с указанным заказом.",
            tags = {"Conversations"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Диалог",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConversationResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Диалог не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.conversation.read')")
    public ResponseEntity<ConversationResponseDto> getConversationByOrderId(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id
    ) {
        return ResponseEntity.ok(conversationsService.getConversationByOrderId(id));
    }

    @PostMapping("/{id}/status")
    @Operation(
            summary = "Изменение статуса заказа",
            description = "Меняет статус заказа. Проверяет допустимость перехода."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус успешно изменён",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Недопустимый переход статуса",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.status.write')")
    public ResponseEntity<Void> changeOrderStatus(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id,
            @RequestBody ClientOrderStatusChangeRequestDto request
    ) {
        ordersService.changeOrderStatus(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/client-approve")
    @Operation(
            summary = "Согласовать заказ (клиент)",
            description = "Утвердить заказ со стороны клиента"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус успешно изменён",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.status.clint-approve')")
    public ResponseEntity<Void> clientApprove(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id
    ) {
        ordersService.clientApprove(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/client-deny")
    @Operation(
            summary = "Отклонить заказ (клиент)",
            description = "Отклонить заказ со стороны клиента"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус успешно изменён",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.status.clint-deny')")
    public ResponseEntity<Void> clientDeny(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id
    ) {
        ordersService.clientDeny(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/price")
    @Operation(
            summary = "Обновить цену заказа",
            description = "Обновляет итоговую стоимость заказа."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Цена обновлена",
                    content = @Content(
                            schema = @Schema(implementation = ClientOrderResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.price.write')")
    public ResponseEntity<ClientOrderResponseDto> updateOrderPrice(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id,
            @RequestBody UpdateOrderPriceRequestDto request
    ) {
        return ResponseEntity.ok(ordersService.updateOrderPrice(id, request));
    }

    @GetMapping("/{id}/materials-consumption")
    @Operation(
            summary = "История расхода материалов по заказу",
            description = "Возвращает записи о расходе материалов по заказу.",
            tags = {"Materials"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Расход материалов",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MaterialConsumptionResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.materials.read')")
    public ResponseEntity<List<MaterialConsumptionResponseDto>> getMaterialsConsumption(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id
    ) {
        return ResponseEntity.ok(materialsService.getMaterialsConsumptionByOrder(id));
    }

    @PatchMapping("/{id}/design/{designId}")
    @Operation(
            summary = "Обновить дизайн заказа",
            description = "Связывает заказ с указанным дизайном."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Дизайн обновлен",
                    content = @Content(
                            schema = @Schema(implementation = ClientOrderResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ или дизайн не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.write')")
    public ResponseEntity<ClientOrderResponseDto> updateOrderDesign(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id,
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long designId
    ) {
        return ResponseEntity.ok(ordersService.updateOrderDesign(id, designId));
    }

    @GetMapping("/{id}/has-status/{status}")
    @Operation(
            summary = "Проверка наличия статуса в истории заказа",
            description = "Проверяет, был ли заказ когда-либо в указанном статусе."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Результат проверки",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_orders.read')")
    public ResponseEntity<Boolean> hasOrderBeenInStatus(
            @PathVariable @Parameter(description = "Идентификатор заказа", required = true) Long id,
            @PathVariable @Parameter(description = "Статус для проверки", required = true) String status
    ) {
        try {
            ru.itmo.se.is.cw.model.value.ClientOrderStatus orderStatus =
                    ru.itmo.se.is.cw.model.value.ClientOrderStatus.valueOf(status);
            return ResponseEntity.ok(ordersService.hasOrderBeenInStatus(id, orderStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }
}
