package com.greenhouse.controller;

import com.greenhouse.dto.BindRequest;
import com.greenhouse.dto.BindingDto;
import com.greenhouse.dto.MeasurementDto;
import com.greenhouse.dto.ModuleDetailsDto;
import com.greenhouse.dto.ModuleRegistrationRequest;
import com.greenhouse.dto.ModuleSummaryDto;
import com.greenhouse.dto.WriteRequest;
import com.greenhouse.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public List<ModuleSummaryDto> list() {
        return moduleService.listModules();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleDetailsDto register(@Valid @RequestBody ModuleRegistrationRequest request) {
        return moduleService.register(request);
    }

    @GetMapping("/{moduleId}")
    public ModuleDetailsDto details(@PathVariable Long moduleId) {
        return moduleService.getDetails(moduleId);
    }

    @PostMapping("/{moduleId}/sync")
    public ModuleDetailsDto sync(@PathVariable Long moduleId) {
        return moduleService.sync(moduleId);
    }

    @PutMapping("/{moduleId}/ports/{portId}/bind")
    public BindingDto bind(@PathVariable Long moduleId,
                           @PathVariable Integer portId,
                           @Valid @RequestBody BindRequest request) {
        return moduleService.bind(moduleId, portId, request.driverId());
    }

    @GetMapping("/{moduleId}/ports/{portId}/bind")
    public Optional<BindingDto> getBinding(@PathVariable Long moduleId,
                                           @PathVariable Integer portId) {
        return moduleService.getBinding(moduleId, portId);
    }

    @GetMapping("/{moduleId}/ports/{portId}/read")
    public MeasurementDto read(@PathVariable Long moduleId,
                               @PathVariable Integer portId) {
        return moduleService.read(moduleId, portId);
    }

    @PostMapping("/{moduleId}/ports/{portId}/write")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void write(@PathVariable Long moduleId,
                      @PathVariable Integer portId,
                      @Valid @RequestBody WriteRequest request) {
        moduleService.write(moduleId, portId, request.level());
    }

    @DeleteMapping("/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long moduleId) {
        moduleService.delete(moduleId);
    }
}
