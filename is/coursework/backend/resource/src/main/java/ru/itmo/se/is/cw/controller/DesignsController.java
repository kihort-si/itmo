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
import ru.itmo.se.is.cw.dto.AddFileToDesignRequest;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.dto.ProductDesignRequestDto;
import ru.itmo.se.is.cw.dto.ProductDesignResponseDto;
import ru.itmo.se.is.cw.dto.filter.ProductDesignFilter;
import ru.itmo.se.is.cw.service.DesignsService;

import java.net.URI;


@RestController
@RequestMapping("/designs")
@Tag(name = "Designs", description = "Операции с дизайнами продуктов")
@RequiredArgsConstructor
public class DesignsController {

    private final DesignsService designsService;

    @GetMapping
    @Operation(
            summary = "Список дизайнов",
            description = "Возвращает пагинированный список дизайнов с возможностью фильтрации."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пагинированный список дизайнов"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.read')")
    public ResponseEntity<Page<ProductDesignResponseDto>> getDesigns(
            @ParameterObject @ModelAttribute ProductDesignFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductDesignResponseDto> designs = designsService.getDesigns(pageable, filter);
        return ResponseEntity.ok(designs);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить дизайн по ID",
            description = "Возвращает дизайн по его идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Дизайн найден",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<ProductDesignResponseDto> getDesignById(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id
    ) {
        ProductDesignResponseDto design = designsService.getDesignById(id);
        return ResponseEntity.ok(design);
    }

    @PostMapping
    @Operation(
            summary = "Создать дизайн",
            description = "Создаёт новый дизайн продукта."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Дизайн создан",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректное тело запроса",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.write')")
    public ResponseEntity<ProductDesignResponseDto> createDesign(
            @RequestBody ProductDesignRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        ProductDesignResponseDto created = designsService.createDesign(request);

        URI location = uriBuilder
                .path("/designs/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить дизайн",
            description = "Обновляет существующий дизайн по ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Дизайн обновлён",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректное тело запроса",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.write')")
    public ResponseEntity<ProductDesignResponseDto> updateDesign(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id,
            @RequestBody ProductDesignRequestDto request
    ) {
        ProductDesignResponseDto updated = designsService.updateDesign(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить дизайн",
            description = "Удаляет дизайн по ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Дизайн удалён",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.delete')")
    public ResponseEntity<Void> deleteDesign(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id
    ) {
        designsService.deleteDesign(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/files")
    @Operation(
            summary = "Добавить файл к дизайну",
            description = "Добавляет файл (3D модель или УП) к существующему дизайну."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Файл добавлен к дизайну",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн или файл не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.write')")
    public ResponseEntity<ProductDesignResponseDto> addFileToDesign(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id,
            @RequestBody AddFileToDesignRequest request
    ) {
        ProductDesignResponseDto updated = designsService.addFileToDesign(id, request.getFileId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/materials")
    @Operation(
            summary = "Добавить материал к дизайну",
            description = "Добавляет требуемый материал к существующему дизайну."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Материал добавлен к дизайну",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн или материал не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.write')")
    public ResponseEntity<ProductDesignResponseDto> addMaterialToDesign(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id,
            @RequestBody ru.itmo.se.is.cw.dto.RequiredMaterialDto materialDto
    ) {
        ProductDesignResponseDto updated = designsService.addMaterialToDesign(id, materialDto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/materials")
    @Operation(
            summary = "Обновить список материалов дизайна",
            description = "Заменяет все материалы дизайна новым списком."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Материалы обновлены",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.write')")
    public ResponseEntity<ProductDesignResponseDto> updateDesignMaterials(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id,
            @RequestBody java.util.List<ru.itmo.se.is.cw.dto.RequiredMaterialDto> materials
    ) {
        ProductDesignResponseDto updated = designsService.updateDesignMaterials(id, materials);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/assign")
    @Operation(
            summary = "Прикрепить конструктора к дизайну",
            description = "Прикрепляет текущего пользователя (конструктора) к дизайну."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Конструктор прикреплён к дизайну",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.write')")
    public ResponseEntity<ProductDesignResponseDto> assignDesigner(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id
    ) {
        ProductDesignResponseDto updated = designsService.assignDesigner(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/files/{fileId}")
    @Operation(
            summary = "Удалить файл из дизайна",
            description = "Удаляет файл из дизайна по идентификатору файла."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Файл удалён из дизайна",
                    content = @Content(schema = @Schema(implementation = ProductDesignResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Дизайн или файл не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_designs.write')")
    public ResponseEntity<ProductDesignResponseDto> removeFileFromDesign(
            @PathVariable @Parameter(description = "Идентификатор дизайна", required = true) Long id,
            @PathVariable @Parameter(description = "Идентификатор файла", required = true) Long fileId
    ) {
        ProductDesignResponseDto updated = designsService.removeFileFromDesign(id, fileId);
        return ResponseEntity.ok(updated);
    }
}
