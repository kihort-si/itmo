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
import ru.itmo.se.is.cw.dto.EmployeeRequestDto;
import ru.itmo.se.is.cw.dto.EmployeeResponseDto;
import ru.itmo.se.is.cw.dto.ProblemDetail;
import ru.itmo.se.is.cw.dto.filter.EmployeeFilter;
import ru.itmo.se.is.cw.service.EmployeesService;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/employees")
@Tag(name = "Employees", description = "Операции на сотрудниками")
public class EmployeesController {

    private final EmployeesService employeesService;

    @PostMapping
    @Operation(
            summary = "Создание сотрудника",
            description = "Создание учетной записи сотрудника, персональных данных и назначение роли."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Сотрудник успешно создан",
                    content = @Content(
                            schema = @Schema(implementation = EmployeeResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
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
    @PreAuthorize("hasAuthority('SCOPE_employees.write')")
    public ResponseEntity<EmployeeResponseDto> createEmployee(
            @RequestBody EmployeeRequestDto request,
            UriComponentsBuilder uriBuilder
    ) {
        EmployeeResponseDto employee = employeesService.createEmployee(request);
        URI location = uriBuilder
                .path("/employees/{id}")
                .buildAndExpand(employee.getId())
                .toUri();
        return ResponseEntity.created(location).body(employee);
    }


    @GetMapping
    @Operation(
            summary = "Список сотрудников",
            description = "Возвращает пагинированную коллекцию сотрудников, с параметрами фильтрации."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Коллекция сотрудников"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный параметр запроса",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
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
    @PreAuthorize("hasAuthority('SCOPE_employees.read')")
    public ResponseEntity<Page<EmployeeResponseDto>> getEmployees(
            @ParameterObject @ModelAttribute EmployeeFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<EmployeeResponseDto> employees = employeesService.getEmployees(pageable, filter);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получение сотрудника",
            description = "Возвращает сотрудника по идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сотрудник найден",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сотрудник не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_employees.read')")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDto employee = employeesService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/by-account/{accountId}")
    @Operation(
            summary = "Получение сотрудника по accountId",
            description = "Возвращает информацию о сотруднике по идентификатору аккаунта."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о сотруднике",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сотрудник не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_employees.read')")
    public ResponseEntity<EmployeeResponseDto> getEmployeeByAccountId(
            @PathVariable @Parameter(description = "Идентификатор аккаунта", required = true) Long accountId
    ) {
        EmployeeResponseDto employee = employeesService.getEmployeeByAccountId(accountId);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/{id}/enable")
    @Operation(
            summary = "Активация аккаунта сотрудника",
            description = "Активирует учетную запись сотрудника по идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Аккаунт сотрудника активирован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сотрудник не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_employees.write')")
    public ResponseEntity<Void> enableEmployee(@PathVariable Long id) {
        employeesService.enableEmployee(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    @Operation(
            summary = "Деактивация аккаунта сотрудника",
            description = "Деактивирует учетную запись сотрудника по идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Аккаунт сотрудника деактивирован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сотрудник не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('SCOPE_employees.write')")
    public ResponseEntity<Void> disableEmployee(@PathVariable Long id) {
        employeesService.disableEmployee(id);
        return ResponseEntity.ok().build();
    }
}
