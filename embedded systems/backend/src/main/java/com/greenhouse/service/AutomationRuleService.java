package com.greenhouse.service;

import com.greenhouse.client.dto.ReadResponse;
import com.greenhouse.dto.AutomationRuleDto;
import com.greenhouse.dto.AutomationRuleRequest;
import com.greenhouse.dto.DtoMapper;
import com.greenhouse.entity.AutomationRuleEntity;
import com.greenhouse.entity.ConditionType;
import com.greenhouse.entity.ModuleEntity;
import com.greenhouse.exception.NotFoundException;
import com.greenhouse.repository.AutomationRuleRepository;
import com.greenhouse.repository.ModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AutomationRuleService {

    private static final Logger log = LoggerFactory.getLogger(AutomationRuleService.class);

    private final AutomationRuleRepository ruleRepository;
    private final ModuleRepository moduleRepository;
    private final EspModuleService espModuleService;

    @Value("${app.rules.off-level:0}")
    private int offLevel;

    public AutomationRuleService(AutomationRuleRepository ruleRepository,
                                  ModuleRepository moduleRepository,
                                  EspModuleService espModuleService) {
        this.ruleRepository = ruleRepository;
        this.moduleRepository = moduleRepository;
        this.espModuleService = espModuleService;
    }


    public List<AutomationRuleDto> listAll() {
        return ruleRepository.findAll().stream()
                .map(DtoMapper::toAutomationRule)
                .toList();
    }

    public AutomationRuleDto getById(Long id) {
        AutomationRuleEntity rule = findRuleOrThrow(id);
        return DtoMapper.toAutomationRule(rule);
    }

    public AutomationRuleDto create(AutomationRuleRequest request) {
        ModuleEntity sourceModule = findModuleOrThrow(request.sourceModuleId());
        ModuleEntity targetModule = findModuleOrThrow(request.targetModuleId());
        ConditionType conditionType = DtoMapper.stringToCondition(request.condition());

        AutomationRuleEntity rule = new AutomationRuleEntity(
                request.name(),
                sourceModule,
                request.sourcePortId(),
                conditionType,
                request.threshold(),
                targetModule,
                request.targetPortId(),
                request.actionLevel(),
                request.enabled()
        );

        rule = ruleRepository.save(rule);
        log.info("Created automation rule id={} name={}", rule.getId(), rule.getName());
        return DtoMapper.toAutomationRule(rule);
    }

    public AutomationRuleDto update(Long id, AutomationRuleRequest request) {
        AutomationRuleEntity rule = findRuleOrThrow(id);

        ModuleEntity sourceModule = findModuleOrThrow(request.sourceModuleId());
        ModuleEntity targetModule = findModuleOrThrow(request.targetModuleId());
        ConditionType conditionType = DtoMapper.stringToCondition(request.condition());

        rule.setName(request.name());
        rule.setSourceModule(sourceModule);
        rule.setSourcePortId(request.sourcePortId());
        rule.setConditionType(conditionType);
        rule.setThreshold(request.threshold());
        rule.setTargetModule(targetModule);
        rule.setTargetPortId(request.targetPortId());
        rule.setActionLevel(request.actionLevel());
        rule.setEnabled(request.enabled());

        rule = ruleRepository.save(rule);
        log.info("Updated automation rule id={}", rule.getId());
        return DtoMapper.toAutomationRule(rule);
    }

    public void delete(Long id) {
        AutomationRuleEntity rule = findRuleOrThrow(id);
        ruleRepository.delete(rule);
        log.info("Deleted automation rule id={}", id);
    }

    public AutomationRuleDto toggle(Long id) {
        AutomationRuleEntity rule = findRuleOrThrow(id);
        rule.setEnabled(!rule.getEnabled());
        rule = ruleRepository.save(rule);
        log.info("Toggled automation rule id={} enabled={}", rule.getId(), rule.getEnabled());
        return DtoMapper.toAutomationRule(rule);
    }

    public int evaluateAllRules() {
        List<AutomationRuleEntity> enabledRules = ruleRepository.findByEnabled(true);
        int triggered = 0;

        for (AutomationRuleEntity rule : enabledRules) {
            try {
                if (evaluateRule(rule)) {
                    triggered++;
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate rule id={}: {}", rule.getId(), e.getMessage());
            }
        }
        return triggered;
    }


    public boolean evaluateRule(Long ruleId) {
        AutomationRuleEntity rule = findRuleOrThrow(ruleId);
        if (!rule.getEnabled()) {
            return false;
        }
        return evaluateRule(rule);
    }

    private boolean evaluateRule(AutomationRuleEntity rule) {
        String sourceBaseUrl = rule.getSourceModule().getBaseUrl();
        int sourcePortId = rule.getSourcePortId();

        ReadResponse readResponse = espModuleService.read(sourceBaseUrl, sourcePortId);
        double value = readResponse.getValue();

        boolean conditionMet = checkCondition(value, rule.getConditionType(), rule.getThreshold());

        String targetBaseUrl = rule.getTargetModule().getBaseUrl();
        int targetPortId = rule.getTargetPortId();

        if (conditionMet) {
            int actionLevel = rule.getActionLevel();
            espModuleService.write(targetBaseUrl, targetPortId, actionLevel);
            log.info("Rule id={} triggered: value={} {} threshold={}, wrote level={} to port={}",
                    rule.getId(), value, DtoMapper.conditionToString(rule.getConditionType()),
                    rule.getThreshold(), actionLevel, targetPortId);
            return true;
        } else {
            // Condition is not met: ensure target is turned off
            espModuleService.write(targetBaseUrl, targetPortId, offLevel);
            log.debug("Rule id={} not triggered: value={} not {} threshold={}, wrote off-level={} to port={}",
                    rule.getId(), value, DtoMapper.conditionToString(rule.getConditionType()),
                    rule.getThreshold(), offLevel, targetPortId);
            return false;
        }
    }

    private boolean checkCondition(double value, ConditionType conditionType, double threshold) {
        return switch (conditionType) {
            case GT -> value > threshold;
            case GTE -> value >= threshold;
            case LT -> value < threshold;
            case LTE -> value <= threshold;
            case EQ -> Math.abs(value - threshold) < 0.0001;
        };
    }

    private AutomationRuleEntity findRuleOrThrow(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Automation rule " + id + " not found"));
    }

    private ModuleEntity findModuleOrThrow(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module " + moduleId + " not found"));
    }
}
