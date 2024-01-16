package com.swisscom.mycoolservice.opa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swisscom.mycoolservice.properties.ApplicationProperties;
import com.swisscom.mycoolservice.properties.ApplicationProperties.User;
import com.swisscom.mycoolservice.servicesimpl.ConfigurationService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class OpaAuthorizationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ConfigurationService configurationService;

    @Value("${opa.swisscom.auth.endpoint.allow}")
    private String opaAuthEndpoint;

    @Autowired
    ApplicationProperties applicationProperties;

    private OpaAuthorizationService subject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        subject = new OpaAuthorizationService(opaAuthEndpoint, configurationService, restTemplate);
    }

    @Test
    void testCheckAuthorizationAuthorized() throws JsonProcessingException {
        String httpMethodType = "POST";
        User adminUser = getUser("adminuser");
        when(configurationService.getCurrentUser()).thenReturn(adminUser);
        when(configurationService.getCurrentUserName()).thenReturn("adminuser");
        String requestBody = subject.buildOpaRequestPayload(httpMethodType, adminUser).getRequestBody();
        when(restTemplate.postForObject(opaAuthEndpoint, requestBody, OpaResponsePayload.class))
                .thenReturn(new OpaResponsePayload(true));

        boolean result = subject.checkAuthorization(httpMethodType);

        assertTrue(result);
        verify(restTemplate, times(1)).postForObject(opaAuthEndpoint, requestBody, OpaResponsePayload.class);
    }

    @Test
    void testCheckAuthorizationUnauthorized() throws JsonProcessingException {
        String httpMethodType = "POST";
        User secadminuser = getUser("secadminuser");
        when(configurationService.getCurrentUser()).thenReturn(secadminuser);
        when(configurationService.getCurrentUserName()).thenReturn("secadminuser");
        String requestBody = subject.buildOpaRequestPayload(httpMethodType, secadminuser).getRequestBody();
        when(restTemplate.postForObject(opaAuthEndpoint, requestBody, OpaResponsePayload.class))
                .thenReturn(new OpaResponsePayload(false));

        boolean result = subject.checkAuthorization(httpMethodType);

        assertFalse(result);
        verify(restTemplate, times(1)).postForObject(opaAuthEndpoint, requestBody, OpaResponsePayload.class);
    }

    @Test
    void testCheckAuthorizationException() throws JsonProcessingException {
        String httpMethodType = "POST";
        User secadminuser = getUser("secadminuser");
        when(configurationService.getCurrentUser()).thenReturn(secadminuser);
        when(configurationService.getCurrentUserName()).thenReturn("secadminuser");
        String requestBody = subject.buildOpaRequestPayload(httpMethodType, secadminuser).getRequestBody();
        when(restTemplate.postForObject(any(), any(), eq(OpaResponsePayload.class)))
                .thenThrow(new RuntimeException("Test Exception"));

        boolean result = subject.checkAuthorization(httpMethodType);

        assertFalse(result);
        verify(restTemplate, times(1)).postForObject(opaAuthEndpoint, requestBody, OpaResponsePayload.class);
    }

    private ApplicationProperties.User getUser(String userName) {
        return applicationProperties.getUsers().get(userName);
    }
}
