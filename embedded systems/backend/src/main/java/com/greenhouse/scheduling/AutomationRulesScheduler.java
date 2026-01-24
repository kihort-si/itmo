package com.greenhouse.scheduling;

import com.greenhouse.service.AutomationRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutomationRulesScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutomationRulesScheduler.class);

    private final AutomationRuleService automationRuleService;

    public AutomationRulesScheduler(AutomationRuleService automationRuleService) {
        this.automationRuleService = automationRuleService;
    }

    @Scheduled(fixedRateString = "${app.rules.scheduler.rate-ms:5000}")
    public void evaluateAll() {
        try {
            int triggered = automationRuleService.evaluateAllRules();
            if (triggered > 0) {
                log.info("Automation rules evaluation: triggered {} rule(s)", triggered);
            } else {
                log.debug("Automation rules evaluation: none triggered");
            }
        } catch (Exception ex) {
            log.warn("Automation rules scheduler failed: {}", ex.getMessage());
        }
    }
}

