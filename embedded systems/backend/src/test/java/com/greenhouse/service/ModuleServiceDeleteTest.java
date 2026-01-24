package com.greenhouse.service;

import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.exception.NotFoundException;
import com.greenhouse.repository.AutomationRuleRepository;
import com.greenhouse.repository.BindingRepository;
import com.greenhouse.repository.DriverRepository;
import com.greenhouse.repository.ModuleRepository;
import com.greenhouse.repository.PortRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceDeleteTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private PortRepository portRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private BindingRepository bindingRepository;

    @Mock
    private AutomationRuleRepository automationRuleRepository;

    @Mock
    private EspModuleService espModuleService;

    @Mock
    private com.greenhouse.repository.MeasurementRepository measurementRepository;

    @Test
    void delete_shouldRemoveModuleAndRelatedData() {
        // given
        ModuleEntity module = new ModuleEntity();
        module.setId(1L);
        module.setModuleUid(100);
        module.setBaseUrl("http://test.local");

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));

        // Create service manually since @InjectMocks doesn't work well with AllArgsConstructor
        ModuleService moduleService = new ModuleService(
                moduleRepository,
                portRepository,
                driverRepository,
                bindingRepository,
                measurementRepository,
                automationRuleRepository,
                espModuleService
        );

        // when
        moduleService.delete(1L);

        // then
        verify(automationRuleRepository).deleteBySourceModule(module);
        verify(automationRuleRepository).deleteByTargetModule(module);
        verify(bindingRepository).deleteByModule(module);
        verify(portRepository).deleteByModule(module);
        verify(driverRepository).deleteByModule(module);
        verify(moduleRepository).delete(module);
    }

    @Test
    void delete_shouldThrowWhenModuleNotFound() {
        // given
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        ModuleService moduleService = new ModuleService(
                moduleRepository,
                portRepository,
                driverRepository,
                bindingRepository,
                measurementRepository,
                automationRuleRepository,
                espModuleService
        );

        // when/then
        assertThatThrownBy(() -> moduleService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Module 999 not found");
    }
}
