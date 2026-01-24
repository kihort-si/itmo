package com.greenhouse.service;

import com.greenhouse.client.dto.BindResponse;
import com.greenhouse.client.dto.DriversResponse;
import com.greenhouse.client.dto.InfoResponse;
import com.greenhouse.client.dto.PortsResponse;
import com.greenhouse.client.dto.ReadResponse;
import com.greenhouse.dto.BindingDto;
import com.greenhouse.dto.DtoMapper;
import com.greenhouse.dto.MeasurementDto;
import com.greenhouse.dto.ModuleDetailsDto;
import com.greenhouse.dto.ModuleRegistrationRequest;
import com.greenhouse.dto.ModuleSummaryDto;
import com.greenhouse.entity.BindingEntity;
import com.greenhouse.entity.DriverEntity;
import com.greenhouse.entity.MeasurementEntity;
import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.entity.PortEntity;
import com.greenhouse.repository.AutomationRuleRepository;
import com.greenhouse.repository.BindingRepository;
import com.greenhouse.repository.DriverRepository;
import com.greenhouse.repository.MeasurementRepository;
import com.greenhouse.repository.ModuleRepository;
import com.greenhouse.repository.PortRepository;
import com.greenhouse.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ModuleService {

    private static final Logger log = LoggerFactory.getLogger(ModuleService.class);

    private final ModuleRepository moduleRepository;
    private final PortRepository portRepository;
    private final DriverRepository driverRepository;
    private final BindingRepository bindingRepository;
    private final MeasurementRepository measurementRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final EspModuleService espModuleService;

    public List<ModuleSummaryDto> listModules() {
        return moduleRepository.findAll()
                .stream()
                .map(DtoMapper::toSummary)
                .toList();
    }

    public ModuleDetailsDto register(ModuleRegistrationRequest request) {
        String baseUrl = normalizeBaseUrl(request.baseUrl());
        InfoResponse info = callInfo(baseUrl);

        ModuleEntity module = moduleRepository.findByModuleUid(info.getModuleId())
                .orElseGet(ModuleEntity::new);
        module.setModuleUid(info.getModuleId());
        module.setBaseUrl(baseUrl);
        module.setName(request.name() != null && !request.name().isBlank()
                ? request.name()
                : defaultName(info.getModuleId(), module.getName()));
        module.setLastSeen(Instant.now());
        module = moduleRepository.save(module);

        return sync(module.getId());
    }

    public ModuleDetailsDto sync(Long moduleId) {
        ModuleEntity module = getModule(moduleId);
        PortsResponse portsResponse = callPorts(module);
        DriversResponse driversResponse = callDrivers(module);

        // Clean previous state and rehydrate from device
        bindingRepository.deleteByModule(module);
        portRepository.deleteByModule(module);
        driverRepository.deleteByModule(module);

        List<PortEntity> savedPorts = new ArrayList<>();
        if (portsResponse != null && portsResponse.getPorts() != null) {
            portsResponse.getPorts().forEach(port -> {
                PortEntity entity = new PortEntity(module, port.getId(), port.getType());
                savedPorts.add(portRepository.save(entity));
            });
        }

        List<DriverEntity> savedDrivers = new ArrayList<>();
        if (driversResponse != null && driversResponse.getDrivers() != null) {
            driversResponse.getDrivers().forEach(driver -> {
                DriverEntity entity = new DriverEntity(module, driver.getId(), driver.getName(), driver.getType());
                savedDrivers.add(driverRepository.save(entity));
            });
        }

        // Try to fetch current bindings from module (optional)
        for (PortEntity port : savedPorts) {
            Optional<BindResponse> bind = callBindSilently(module, port.getPortId());
            bind.ifPresent(bindResponse -> {
                DriverEntity driver = findDriverOrThrow(module, bindResponse.getDriverId());
                bindingRepository.save(new BindingEntity(module, port, driver));
            });
        }

        module.setLastSeen(Instant.now());
        moduleRepository.save(module);

        return buildDetails(module);
    }

    public ModuleDetailsDto getDetails(Long moduleId) {
        ModuleEntity module = getModule(moduleId);
        return buildDetails(module);
    }

    public BindingDto bind(Long moduleId, Integer portId, Integer driverId) {
        ModuleEntity module = getModule(moduleId);
        PortEntity port = findPortOrThrow(module, portId);
        DriverEntity driver = findDriverOrThrow(module, driverId);

        try {
            espModuleService.bind(module.getBaseUrl(), portId, driverId);
        } catch (HttpStatusCodeException ex) {
            throw ex;
        }

        bindingRepository.findByPort(port).ifPresent(bindingRepository::delete);
        BindingEntity saved = bindingRepository.save(new BindingEntity(module, port, driver));
        module.setLastSeen(Instant.now());
        moduleRepository.save(module);
        return DtoMapper.toBinding(saved);
    }

    public Optional<BindingDto> getBinding(Long moduleId, Integer portId) {
        ModuleEntity module = getModule(moduleId);
        PortEntity port = findPortOrThrow(module, portId);
        return bindingRepository.findByPort(port).map(DtoMapper::toBinding);
    }

    public MeasurementDto read(Long moduleId, Integer portId) {
        ModuleEntity module = getModule(moduleId);
        PortEntity port = findPortOrThrow(module, portId);
        BindingEntity binding = bindingRepository.findByPort(port).orElse(null);

        ReadResponse response;
        try {
            response = espModuleService.read(module.getBaseUrl(), portId);
        } catch (HttpStatusCodeException ex) {
            throw ex;
        }

        DriverEntity driver = binding != null ? binding.getDriver() : null;
        MeasurementEntity measurement = new MeasurementEntity(module, port, driver, response.getValue());
        measurementRepository.save(measurement);
        module.setLastSeen(Instant.now());
        moduleRepository.save(module);
        return DtoMapper.toMeasurement(measurement);
    }

    public void write(Long moduleId, Integer portId, Integer level) {
        ModuleEntity module = getModule(moduleId);
        findPortOrThrow(module, portId);
        try {
            espModuleService.write(module.getBaseUrl(), portId, level);
        } catch (HttpStatusCodeException ex) {
            throw ex;
        }
        module.setLastSeen(Instant.now());
        moduleRepository.save(module);
    }

    /**
     * Удалить модуль и все связанные данные (порты, драйверы, привязки, измерения, правила автоматизации).
     */
    public void delete(Long moduleId) {
        ModuleEntity module = getModule(moduleId);

        // Удаляем правила автоматизации, связанные с этим модулем
        automationRuleRepository.deleteBySourceModule(module);
        automationRuleRepository.deleteByTargetModule(module);

        // Удаляем привязки, порты, драйверы (cascade должен работать, но явно для надёжности)
        bindingRepository.deleteByModule(module);
        portRepository.deleteByModule(module);
        driverRepository.deleteByModule(module);

        // Удаляем сам модуль
        moduleRepository.delete(module);
        log.info("Deleted module id={}", moduleId);
    }

    private ModuleEntity getModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module " + moduleId + " not found"));
    }

    private PortEntity findPortOrThrow(ModuleEntity module, Integer portId) {
        return portRepository.findByModuleAndPortId(module, portId)
                .orElseThrow(() -> new NotFoundException("Port " + portId + " not found for module " + module.getId()));
    }

    private DriverEntity findDriverOrThrow(ModuleEntity module, Integer driverId) {
        return driverRepository.findByModuleAndDriverId(module, driverId)
                .orElseThrow(() -> new NotFoundException("Driver " + driverId + " not found for module " + module.getId()));
    }

    private Optional<BindResponse> callBindSilently(ModuleEntity module, Integer portId) {
        try {
            return espModuleService.getBinding(module.getBaseUrl(), portId);
        } catch (HttpStatusCodeException ex) {
            return Optional.empty();
        }
    }

    private InfoResponse callInfo(String baseUrl) {
        try {
            return espModuleService.getInfo(baseUrl);
        } catch (HttpStatusCodeException ex) {
            throw ex;
        }
    }

    private PortsResponse callPorts(ModuleEntity module) {
        try {
            return espModuleService.getPorts(module.getBaseUrl());
        } catch (HttpStatusCodeException ex) {
            throw ex;
        }
    }

    private DriversResponse callDrivers(ModuleEntity module) {
        try {
            return espModuleService.getDrivers(module.getBaseUrl());
        } catch (HttpStatusCodeException ex) {
            throw ex;
        }
    }

    private ModuleDetailsDto buildDetails(ModuleEntity module) {
        List<PortEntity> ports = portRepository.findByModule(module);
        List<DriverEntity> drivers = driverRepository.findByModule(module);
        List<BindingEntity> bindings = bindingRepository.findByModule(module);
        return DtoMapper.toDetails(module, ports, drivers, bindings);
    }

    private String normalizeBaseUrl(String url) {
        String normalized = url.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String defaultName(Integer moduleId, String existingName) {
        if (existingName != null && !existingName.isBlank()) {
            return existingName;
        }
        return "module-" + moduleId;
    }
}
