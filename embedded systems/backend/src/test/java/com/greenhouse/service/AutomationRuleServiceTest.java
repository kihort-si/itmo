package com.greenhouse.service;

import com.greenhouse.client.dto.ReadResponse;
import com.greenhouse.dto.AutomationRuleDto;
import com.greenhouse.dto.AutomationRuleRequest;
import com.greenhouse.entity.AutomationRuleEntity;
import com.greenhouse.entity.ConditionType;
import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.exception.NotFoundException;
import com.greenhouse.repository.AutomationRuleRepository;
import com.greenhouse.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationRuleServiceTest {

    @Mock
    private AutomationRuleRepository ruleRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private EspModuleService espModuleService;

    private AutomationRuleService ruleService;

    private ModuleEntity sourceModule;
    private ModuleEntity targetModule;

    @BeforeEach
    void setUp() {
        ruleService = new AutomationRuleService(ruleRepository, moduleRepository, espModuleService);

        sourceModule = new ModuleEntity();
        sourceModule.setId(1L);
        sourceModule.setModuleUid(100);
        sourceModule.setBaseUrl("http://source.local");

        targetModule = new ModuleEntity();
        targetModule.setId(2L);
        targetModule.setModuleUid(200);
        targetModule.setBaseUrl("http://target.local");
    }

    @Test
    void create_shouldSaveAndReturnRule() {
        // given
        AutomationRuleRequest request = new AutomationRuleRequest(
                "Test rule",
                1L, 1,
                "gt", 25.0,
                2L, 2,
                128, true
        );

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(sourceModule));
        when(moduleRepository.findById(2L)).thenReturn(Optional.of(targetModule));
        when(ruleRepository.save(any(AutomationRuleEntity.class))).thenAnswer(invocation -> {
            AutomationRuleEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        // when
        AutomationRuleDto result = ruleService.create(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Test rule");
        assertThat(result.condition()).isEqualTo("gt");
        assertThat(result.threshold()).isEqualTo(25.0);
        assertThat(result.actionLevel()).isEqualTo(128);
        assertThat(result.enabled()).isTrue();

        verify(ruleRepository).save(any(AutomationRuleEntity.class));
    }

    @Test
    void create_shouldThrowWhenSourceModuleNotFound() {
        // given
        AutomationRuleRequest request = new AutomationRuleRequest(
                "Test rule",
                999L, 1,
                "gt", 25.0,
                2L, 2,
                128, true
        );

        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> ruleService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Module 999 not found");
    }

    @Test
    void toggle_shouldFlipEnabledFlag() {
        // given
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setId(1L);
        rule.setName("Test");
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(1);
        rule.setConditionType(ConditionType.GT);
        rule.setThreshold(25.0);
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(2);
        rule.setActionLevel(128);
        rule.setEnabled(true);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(AutomationRuleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AutomationRuleDto result = ruleService.toggle(1L);

        // then
        assertThat(result.enabled()).isFalse();
    }

    @Test
    void evaluateRule_shouldTriggerAction_whenConditionMet_GT() {
        // given
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setId(1L);
        rule.setName("High temp rule");
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(1);
        rule.setConditionType(ConditionType.GT);
        rule.setThreshold(25.0);
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(2);
        rule.setActionLevel(255);
        rule.setEnabled(true);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        ReadResponse readResponse = new ReadResponse();
        readResponse.setValue(30.0); // > 25.0, condition met
        when(espModuleService.read("http://source.local", 1)).thenReturn(readResponse);

        // when
        boolean triggered = ruleService.evaluateRule(1L);

        // then
        assertThat(triggered).isTrue();
        verify(espModuleService).write("http://target.local", 2, 255);
    }

    @Test
    void evaluateRule_shouldNotTrigger_whenConditionNotMet_GT() {
        // given
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setId(1L);
        rule.setName("High temp rule");
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(1);
        rule.setConditionType(ConditionType.GT);
        rule.setThreshold(25.0);
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(2);
        rule.setActionLevel(255);
        rule.setEnabled(true);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        ReadResponse readResponse = new ReadResponse();
        readResponse.setValue(20.0); // < 25.0, condition NOT met
        when(espModuleService.read("http://source.local", 1)).thenReturn(readResponse);

        // when
        boolean triggered = ruleService.evaluateRule(1L);

        // then
        assertThat(triggered).isFalse();
        verify(espModuleService, never()).write(anyString(), anyInt(), anyInt());
    }

    @Test
    void evaluateRule_shouldNotTrigger_whenRuleDisabled() {
        // given
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setId(1L);
        rule.setName("Disabled rule");
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(1);
        rule.setConditionType(ConditionType.GT);
        rule.setThreshold(25.0);
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(2);
        rule.setActionLevel(255);
        rule.setEnabled(false); // disabled

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        // when
        boolean triggered = ruleService.evaluateRule(1L);

        // then
        assertThat(triggered).isFalse();
        verify(espModuleService, never()).read(anyString(), anyInt());
        verify(espModuleService, never()).write(anyString(), anyInt(), anyInt());
    }

    @Test
    void evaluateRule_LT_condition() {
        // given
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setId(1L);
        rule.setName("Low temp rule");
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(1);
        rule.setConditionType(ConditionType.LT);
        rule.setThreshold(15.0);
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(2);
        rule.setActionLevel(100);
        rule.setEnabled(true);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        ReadResponse readResponse = new ReadResponse();
        readResponse.setValue(10.0); // < 15.0, condition met
        when(espModuleService.read("http://source.local", 1)).thenReturn(readResponse);

        // when
        boolean triggered = ruleService.evaluateRule(1L);

        // then
        assertThat(triggered).isTrue();
        verify(espModuleService).write("http://target.local", 2, 100);
    }

    @Test
    void evaluateRule_EQ_condition() {
        // given
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setId(1L);
        rule.setName("Exact temp rule");
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(1);
        rule.setConditionType(ConditionType.EQ);
        rule.setThreshold(22.0);
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(2);
        rule.setActionLevel(50);
        rule.setEnabled(true);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        ReadResponse readResponse = new ReadResponse();
        readResponse.setValue(22.0); // == 22.0, condition met
        when(espModuleService.read("http://source.local", 1)).thenReturn(readResponse);

        // when
        boolean triggered = ruleService.evaluateRule(1L);

        // then
        assertThat(triggered).isTrue();
        verify(espModuleService).write("http://target.local", 2, 50);
    }

    @Test
    void evaluateAllRules_shouldEvaluateOnlyEnabledRules() {
        // given
        AutomationRuleEntity enabledRule = new AutomationRuleEntity();
        enabledRule.setId(1L);
        enabledRule.setName("Enabled rule");
        enabledRule.setSourceModule(sourceModule);
        enabledRule.setSourcePortId(1);
        enabledRule.setConditionType(ConditionType.GT);
        enabledRule.setThreshold(20.0);
        enabledRule.setTargetModule(targetModule);
        enabledRule.setTargetPortId(2);
        enabledRule.setActionLevel(200);
        enabledRule.setEnabled(true);

        when(ruleRepository.findByEnabled(true)).thenReturn(List.of(enabledRule));

        ReadResponse readResponse = new ReadResponse();
        readResponse.setValue(25.0); // > 20.0, condition met
        when(espModuleService.read("http://source.local", 1)).thenReturn(readResponse);

        // when
        int triggeredCount = ruleService.evaluateAllRules();

        // then
        assertThat(triggeredCount).isEqualTo(1);
        verify(espModuleService).write("http://target.local", 2, 200);
    }

    @Test
    void delete_shouldRemoveRule() {
        // given
        AutomationRuleEntity rule = new AutomationRuleEntity();
        rule.setId(1L);
        rule.setName("To delete");
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(1);
        rule.setConditionType(ConditionType.GT);
        rule.setThreshold(25.0);
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(2);
        rule.setActionLevel(128);
        rule.setEnabled(true);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        // when
        ruleService.delete(1L);

        // then
        verify(ruleRepository).delete(rule);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        // given
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> ruleService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Automation rule 999 not found");
    }
}
