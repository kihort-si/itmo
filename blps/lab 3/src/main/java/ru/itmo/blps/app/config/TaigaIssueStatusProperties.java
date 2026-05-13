package ru.itmo.blps.app.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.taiga")
public class TaigaIssueStatusProperties {

    /**
     * Key: {@link ru.itmo.blps.app.models.enums.OrderStatus} name.
     * Value: Taiga issue workflow {@code status} id for this project (see GET /api/v1/issues/filters_data?project=…).
     */
    private Map<String, Integer> issueStatusByOrderStatus = new HashMap<>();

    public Map<String, Integer> getIssueStatusByOrderStatus() {
        return issueStatusByOrderStatus;
    }

    public void setIssueStatusByOrderStatus(Map<String, Integer> issueStatusByOrderStatus) {
        this.issueStatusByOrderStatus =
                issueStatusByOrderStatus != null ? issueStatusByOrderStatus : new HashMap<>();
    }
}
