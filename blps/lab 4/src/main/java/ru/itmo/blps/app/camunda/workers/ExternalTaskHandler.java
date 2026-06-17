package ru.itmo.blps.app.camunda.workers;

import java.util.Map;

public interface ExternalTaskHandler {

    String topic();

    Map<String, Object> handle(Map<String, Object> variables) throws Exception;
}
