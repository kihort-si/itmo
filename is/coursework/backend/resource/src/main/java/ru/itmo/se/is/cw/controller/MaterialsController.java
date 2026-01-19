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
import ru.itmo.se.is.cw.dto.filter.MaterialFilter;
import ru.itmo.se.is.cw.service.MaterialsService;

import java.net.URI;


@RestController
@RequestMapping("/materials")
@Tag(name = "Materials", description = "Операции с материалами")
@RequiredArgsConstructor
public class MaterialsController {

    private final MaterialsService materialsService;

    @GetMapping
    @Operation(
            summary = "Список материалов",
            description = "Возвращает список всех доступных материалов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Материалы",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = MaterialResponseDto.class))
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_materials.read')")
    public ResponseEntity<Page<MaterialResponseDto>> getMaterials(
            @ParameterObject @ModelAttribute MaterialFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<MaterialResponseDto> materials = materialsService.getMaterials(pageable, filter);
        return ResponseEntity.ok(materials);
    }


    @PostMapping
    @Operation(
            summary = "Создать материал (админ/снабжение)",
            description = "Создает новый материал. Доступно администраторам и сотрудникам снабжения."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Материал создан",
                    content = @Content(
                            schema = @Schema(implementation = MaterialResponseDto.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_materials.write')")
    public ResponseEntity<MaterialResponseDto> createMaterial(
            @RequestBody MaterialRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        MaterialResponseDto material = materialsService.createMaterial(request);
        URI location = uriBuilder
                .path("/materials/{id}")
                .buildAndExpand(material.getId())
                .toUri();
        return ResponseEntity.created(location).body(material);
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Детали материала",
            description = "Возвращает информацию о материале по его идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Материал найден",
                    content = @Content(
                            schema = @Schema(implementation = MaterialResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Материал не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<MaterialResponseDto> getMaterialById(
            @PathVariable @Parameter(description = "Идентификатор материала", required = true) Long id
    ) {
        MaterialResponseDto material = materialsService.getMaterialById(id);
        return ResponseEntity.ok(material);
    }


    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить материал (админ/снабжение)",
            description = "Обновляет информацию о существующем материале."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Материал обновлён",
                    content = @Content(
                            schema = @Schema(implementation = MaterialResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Материал не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_materials.write')")
    public ResponseEntity<MaterialResponseDto> updateMaterial(
            @PathVariable @Parameter(description = "Идентификатор материала", required = true) Long id,

            @RequestBody MaterialRequestDto request
    ) {
        MaterialResponseDto material = materialsService.updateMaterial(id, request);
        return ResponseEntity.ok(material);
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить материал (админ)",
            description = "Удаляет материал по идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Материал удалён",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Материал не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_materials.delete')")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable @Parameter(description = "Идентификатор материала", required = true) Long id
    ) {
        materialsService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/balance")
    @Operation(
            summary = "Установить текущий баланс материала (склад/снабжение)",
            description = "Устанавливает новый текущий баланс материала. Создаёт запись в истории баланса и обновляет currentBalance."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Баланс обновлён",
                    content = @Content(schema = @Schema(implementation = MaterialResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Материал не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_materials.balance.write')")
    public ResponseEntity<MaterialResponseDto> setBalance(
            @PathVariable @Parameter(description = "Идентификатор материала", required = true) Long id,
            @RequestBody MaterialBalanceChangeRequestDto request
    ) {
        MaterialResponseDto material = materialsService.updateMaterialBalance(id, request.getAmount());
        return ResponseEntity.ok(material);
    }

    @GetMapping("/{id}/balance-history")
    @Operation(
            summary = "История изменений остатка материала",
            description = "Возвращает историю движения и баланса материала."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "История балансов/списаний",
                    content = @Content(
                            schema = @Schema(implementation = MaterialBalanceHistoryResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Материал не найден",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_materials.balance.read')")
    public ResponseEntity<MaterialBalanceHistoryResponseDto> getMaterialBalanceHistory(
            @PathVariable @Parameter(description = "Идентификатор материала", required = true) Long id
    ) {
        MaterialBalanceHistoryResponseDto history = materialsService.getMaterialBalanceHistory(id);
        return ResponseEntity.ok(history);
    }
}
