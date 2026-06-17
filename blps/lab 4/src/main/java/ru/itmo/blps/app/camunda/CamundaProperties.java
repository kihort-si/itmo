package ru.itmo.blps.app.camunda;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.camunda")
public record CamundaProperties(
        String restApiUrl,
        String workerId,
        long lockDurationMs,
        int maxTasksPerPoll,
        long pollIntervalMs,
        String processDefinitionKey,
        String deploymentName
) {
    public CamundaProperties {
        if (restApiUrl == null || restApiUrl.isBlank()) {
            restApiUrl = "http://localhost:8080/engine-rest";
        }
        if (workerId == null || workerId.isBlank()) {
            workerId = "mts-shop-worker";
        }
        if (lockDurationMs <= 0) lockDurationMs = 30_000L;
        if (maxTasksPerPoll <= 0) maxTasksPerPoll = 5;
        if (pollIntervalMs <= 0) pollIntervalMs = 5_000L;
        if (processDefinitionKey == null || processDefinitionKey.isBlank()) {
            processDefinitionKey = "mts-order-process";
        }
        if (deploymentName == null || deploymentName.isBlank()) {
            deploymentName = "MTS eShop Order Process";
        }
    }
}
