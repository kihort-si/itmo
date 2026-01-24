package com.greenhouse.controller;

import com.greenhouse.dto.AutomationRuleDto;
import com.greenhouse.dto.AutomationRuleRequest;
import com.greenhouse.service.AutomationRuleService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class AutomationRuleController {

    private final AutomationRuleService ruleService;

    public AutomationRuleController(AutomationRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public List<AutomationRuleDto> list() {
        return ruleService.listAll();
    }

    @GetMapping("/{id}")
    public AutomationRuleDto getById(@PathVariable Long id) {
        return ruleService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutomationRuleDto create(@Valid @RequestBody AutomationRuleRequest request) {
        return ruleService.create(request);
    }

    @PutMapping("/{id}")
    public AutomationRuleDto update(@PathVariable Long id,
                                    @Valid @RequestBody AutomationRuleRequest request) {
        return ruleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ruleService.delete(id);
    }

    @PostMapping("/{id}/toggle")
    public AutomationRuleDto toggle(@PathVariable Long id) {
        return ruleService.toggle(id);
    }

    @PostMapping("/{id}/evaluate")
    public Map<String, Object> evaluateOne(@PathVariable Long id) {
        boolean triggered = ruleService.evaluateRule(id);
        return Map.of("ruleId", id, "triggered", triggered);
    }

    @PostMapping("/evaluate")
    public Map<String, Object> evaluateAll() {
        int triggeredCount = ruleService.evaluateAllRules();
        return Map.of("triggeredCount", triggeredCount);
    }
}
