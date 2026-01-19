package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.dto.ProductionTaskResponseDto;
import ru.itmo.se.is.cw.service.ProductionService;


@RestController
@RequestMapping("/production-tasks")
@Tag(name = "Production", description = "Операции с производственными задачами")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @GetMapping
    @Operation(
            summary = "Текущая пороизводственная задача"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Задача найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProductionTaskResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Задача не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_production.read')")
    public ResponseEntity<ProductionTaskResponseDto> getProductionTaskById() {
        ProductionTaskResponseDto productionTask = productionService.getCurrentTask();
        return ResponseEntity.ok(productionTask);
    }

    @PostMapping("/{id}/start")
    @Operation(
            summary = "Старт задачи на производстве",
            description = "Переводит задачу в состояние выполнения."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Задача успешно запущена",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Задача не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_production.execute')")
    public ResponseEntity<Void> startProductionTask(
            @PathVariable @Parameter(description = "Идентификатор задачи", required = true) Long id
    ) {
        productionService.startProductionTask(id);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/finish")
    @Operation(
            summary = "Завершение задачи на производстве",
            description = "Переводит задачу в состояние завершения."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Задача успешно завершена",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Задача не найдена",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_production.execute')")
    public ResponseEntity<Void> finishProductionTask(
            @PathVariable @Parameter(description = "Идентификатор задачи", required = true) Long id
    ) {
        productionService.finishProductionTask(id);
        return ResponseEntity.ok().build();
    }
}
