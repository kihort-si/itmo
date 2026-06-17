package ru.itmo.blps.app.camunda;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CamundaDeploymentService {

    private static final Logger log = LoggerFactory.getLogger(CamundaDeploymentService.class);

    private static final String BPMN = "bpmn/order-process.bpmn";
    private static final List<String> FORMS = List.of(
            "bpmn/forms/checkout.form",
            "bpmn/forms/prepare-pickup.form",
            "bpmn/forms/assign-courier.form",
            "bpmn/forms/confirm-pickup.form",
            "bpmn/forms/courier-pickup.form",
            "bpmn/forms/deliver.form"
    );

    private final RestClient camundaRestClient;
    private final CamundaProperties props;

    public CamundaDeploymentService(RestClient camundaRestClient, CamundaProperties props) {
        this.camundaRestClient = camundaRestClient;
        this.props = props;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deploy() {
        log.info("Deploying BPMN process and forms to Camunda at {}", props.restApiUrl());
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("deployment-name", props.deploymentName());
            body.add("enable-duplicate-filtering", "true");
            body.add("deploy-changed-only", "true");

            addResource(body, BPMN);
            for (String form : FORMS) {
                addResource(body, form);
            }

            String response = camundaRestClient.post()
                    .uri("/deployment/create")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("Camunda deployment successful: {}", response);
        } catch (RestClientException e) {
            log.error("Failed to deploy process to Camunda. Workers will not function until Camunda is available.", e);
        } catch (IOException e) {
            log.error("Failed to read BPMN/form resource from classpath", e);
        }
    }

    private void addResource(MultiValueMap<String, Object> body, String classpathPath) throws IOException {
        Resource resource = new ClassPathResource(classpathPath);
        if (!resource.exists()) {
            log.warn("Resource not found on classpath: {}", classpathPath);
            return;
        }
        String filename = resource.getFilename();
        body.add(filename, resource);
    }
}
