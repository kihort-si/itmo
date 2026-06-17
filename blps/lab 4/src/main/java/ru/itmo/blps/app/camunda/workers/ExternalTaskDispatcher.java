package ru.itmo.blps.app.camunda.workers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.itmo.blps.app.camunda.CamundaProperties;

@Component
public class ExternalTaskDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ExternalTaskDispatcher.class);
    private static final int DEFAULT_RETRIES = 3;
    private static final long RETRY_TIMEOUT_MS = 10_000L;

    private final RestClient camundaRestClient;
    private final CamundaProperties props;
    private final ObjectMapper objectMapper;
    private final Map<String, ExternalTaskHandler> handlersByTopic;

    public ExternalTaskDispatcher(RestClient camundaRestClient,
                                  CamundaProperties props,
                                  ObjectMapper objectMapper,
                                  List<ExternalTaskHandler> handlers) {
        this.camundaRestClient = camundaRestClient;
        this.props = props;
        this.objectMapper = objectMapper;
        this.handlersByTopic = new HashMap<>();
        for (ExternalTaskHandler h : handlers) {
            handlersByTopic.put(h.topic(), h);
            log.info("Registered external task handler for topic '{}'", h.topic());
        }
    }

    @Scheduled(fixedDelayString = "${app.camunda.poll-interval-ms:5000}",
               initialDelayString = "${app.camunda.initial-delay-ms:15000}")
    public void poll() {
        if (handlersByTopic.isEmpty()) return;
        try {
            List<Map<String, Object>> tasks = fetchAndLock();
            for (Map<String, Object> task : tasks) {
                processTask(task);
            }
        } catch (RestClientException e) {
            log.debug("Camunda not reachable during poll: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during external task poll", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchAndLock() {
        List<Map<String, Object>> topics = new ArrayList<>();
        for (String topic : handlersByTopic.keySet()) {
            topics.add(Map.of(
                    "topicName", topic,
                    "lockDuration", props.lockDurationMs()
            ));
        }

        Map<String, Object> body = Map.of(
                "workerId", props.workerId(),
                "maxTasks", props.maxTasksPerPoll(),
                "usePriority", false,
                "topics", topics
        );

        String json = camundaRestClient.post()
                .uri("/external-task/fetchAndLock")
                .body(body)
                .retrieve()
                .body(String.class);

        if (json == null || json.isBlank() || json.equals("[]")) return List.of();

        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse fetchAndLock response: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private void processTask(Map<String, Object> task) {
        String taskId   = (String) task.get("id");
        String topic    = (String) task.get("topicName");
        int retriesLeft = task.get("retries") instanceof Number n ? n.intValue() : DEFAULT_RETRIES;

        ExternalTaskHandler handler = handlersByTopic.get(topic);
        if (handler == null) {
            log.warn("No handler found for topic '{}', releasing task {}", topic, taskId);
            return;
        }

        Map<String, Object> rawVars = (Map<String, Object>) task.getOrDefault("variables", Map.of());
        Map<String, Object> vars    = unwrapVariables(rawVars);

        try {
            Map<String, Object> outputVars = handler.handle(vars);
            completeTask(taskId, outputVars);
            log.info("Completed external task id={} topic={}", taskId, topic);
        } catch (Exception e) {
            log.error("External task handler failed: topic={} taskId={}", topic, taskId, e);
            int nextRetries = Math.max(retriesLeft - 1, 0);
            reportFailure(taskId, e.getMessage(), nextRetries, RETRY_TIMEOUT_MS);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrapVariables(Map<String, Object> raw) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            Object v = entry.getValue();
            if (v instanceof Map<?,?> typedVar) {
                result.put(entry.getKey(), ((Map<String, Object>) typedVar).get("value"));
            } else {
                result.put(entry.getKey(), v);
            }
        }
        return result;
    }

    private void completeTask(String taskId, Map<String, Object> outputVars) {
        Map<String, Object> wrappedVars = new HashMap<>();
        for (Map.Entry<String, Object> e : outputVars.entrySet()) {
            if (e.getValue() != null) {
                wrappedVars.put(e.getKey(), Map.of("value", e.getValue()));
            }
        }
        Map<String, Object> body = Map.of(
                "workerId", props.workerId(),
                "variables", wrappedVars
        );
        camundaRestClient.post()
                .uri("/external-task/{id}/complete", taskId)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void reportFailure(String taskId, String errorMessage, int retries, long retryTimeout) {
        Map<String, Object> body = Map.of(
                "workerId", props.workerId(),
                "errorMessage", errorMessage != null ? errorMessage : "Unknown error",
                "retries", retries,
                "retryTimeout", retryTimeout
        );
        try {
            camundaRestClient.post()
                    .uri("/external-task/{id}/failure", taskId)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Could not report failure for task {}: {}", taskId, e.getMessage());
        }
    }
}
