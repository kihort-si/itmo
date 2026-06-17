package ru.itmo.blps.app.camunda;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CamundaProcessFacade {

    private static final Logger log = LoggerFactory.getLogger(CamundaProcessFacade.class);

    private final RestClient camundaRestClient;
    private final CamundaProperties props;

    public CamundaProcessFacade(RestClient camundaRestClient, CamundaProperties props) {
        this.camundaRestClient = camundaRestClient;
        this.props = props;
    }

    public String startOrderProcess(Long customerId,
                                    String promoCode,
                                    String fulfillmentMethod,
                                    String paymentMethod,
                                    Long shopId,
                                    String deliveryAddress) {
        Map<String, Object> variables = buildVariables(
                customerId, promoCode, fulfillmentMethod, paymentMethod, shopId, deliveryAddress);

        Map<String, Object> body = Map.of(
                "variables", variables,
                "businessKey", "order-customer-" + customerId
        );

        try {
            StartProcessResponse response = camundaRestClient.post()
                    .uri("/process-definition/key/{key}/start", props.processDefinitionKey())
                    .body(body)
                    .retrieve()
                    .body(StartProcessResponse.class);

            String instanceId = response != null ? response.id() : null;
            log.info("Started Camunda process instance {} for customerId={}", instanceId, customerId);
            return instanceId;
        } catch (RestClientException e) {
            log.error("Failed to start Camunda process for customerId={}", customerId, e);
            throw new RuntimeException("Не удалось запустить процесс оформления заказа", e);
        }
    }

    private Map<String, Object> buildVariables(Long customerId, String promoCode,
                                                String fulfillmentMethod, String paymentMethod,
                                                Long shopId, String deliveryAddress) {
        var vars = new LinkedHashMap<String, Object>();
        vars.put("customerId",        camundaVar(customerId,        "Long"));
        vars.put("fulfillmentMethod", camundaVar(fulfillmentMethod, "String"));
        vars.put("paymentMethod",     camundaVar(paymentMethod,     "String"));
        if (promoCode != null && !promoCode.isBlank()) {
            vars.put("promoCode", camundaVar(promoCode, "String"));
        }
        if (shopId != null) {
            vars.put("shopId", camundaVar(shopId, "Long"));
        }
        if (deliveryAddress != null && !deliveryAddress.isBlank()) {
            vars.put("deliveryAddress", camundaVar(deliveryAddress, "String"));
        }
        return vars;
    }

    private Map<String, Object> camundaVar(Object value, String type) {
        return Map.of("value", value, "type", type);
    }

    record StartProcessResponse(String id, String definitionId, String businessKey) {}
}
