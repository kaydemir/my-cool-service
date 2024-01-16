package com.swisscom.mycoolservice.opa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.mycoolservice.properties.ApplicationProperties.User;
import com.swisscom.mycoolservice.servicesimpl.ConfigurationService;
/**
 * Service class for handling authorization checks using Open Policy Agent (OPA).
 */
@Service
public class OpaAuthorizationService {

    protected static final Logger logger = LogManager.getLogger(OpaAuthorizationService.class);

    private final String opaAuthEndpoint;
    private final ConfigurationService configurationService;
    private final RestTemplate restTemplate;

    @Autowired
    public OpaAuthorizationService(
            @Value("${opa.swisscom.auth.endpoint.allow}") String opaAuthEndpoint,
            ConfigurationService configurationService,
            RestTemplate restTemplate) {
        this.opaAuthEndpoint = opaAuthEndpoint;
        this.configurationService = configurationService;
        this.restTemplate = restTemplate;
    }

    public boolean checkAuthorization(String httpMethodType) {
        // Get authenticated user's information
        User currentUser = configurationService.getCurrentUser();
        String currentUserName = configurationService.getCurrentUserName();
        // Create a request payload for OPA
        try {
            OpaRequestPayload opaRequestPayload = buildOpaRequestPayload(httpMethodType, currentUser);
            // Send the request to OPA and parse the response
            String requestBody = opaRequestPayload.getRequestBody();
            logger.info("Authorization Request URL: {}  Payload: {}", opaAuthEndpoint, requestBody);
            OpaResponsePayload opaResponsePayload = restTemplate.postForObject(opaAuthEndpoint, requestBody, OpaResponsePayload.class);
            if (opaResponsePayload == null) {
                logger.warn("Cannot receive response for user '{}' authorization failure.", currentUserName);
                return false;
            }
            logger.info("Authorization Response {}", opaResponsePayload.isResult());
            // Evaluate the decision from OPA response
            boolean isAuthorized = opaResponsePayload.isResult();
            if (isAuthorized) {
                logger.info("User '{}' authorization success", currentUserName);
            } else {
                logger.info("User '{}' authorization failure", currentUserName);
            }
            return isAuthorized;
        } catch (Exception e) {
            logger.error("Exception while sending authorization request. Error Details:  ", e);
            return false;
        }
    }

    OpaRequestPayload buildOpaRequestPayload(String httpMethodType, User user) throws JsonProcessingException {

        OpaRequestPayload opaRequestPayload = new OpaRequestPayload();
        opaRequestPayload.setRequestBody(constructJsonInput(httpMethodType, user.getConfig().getRoles()));

        return opaRequestPayload;
    }

    private static String constructJsonInput(String httpMethodType, String[] roles) throws JsonProcessingException {
        // Construct the input JSON using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // Create a Map to represent the input structure
        // You can use a custom class if needed
        // For simplicity, using a Map here
        java.util.Map<String, Object> inputMap = new java.util.HashMap<>();
        inputMap.put("method", httpMethodType);
        inputMap.put("roles", java.util.Arrays.asList(roles));

        // Create a Map to represent the overall structure
        java.util.Map<String, Object> overallMap = new java.util.HashMap<>();
        overallMap.put("input", inputMap);

        // Convert the Map to a JSON string
        return objectMapper.writeValueAsString(overallMap);
    }

}
