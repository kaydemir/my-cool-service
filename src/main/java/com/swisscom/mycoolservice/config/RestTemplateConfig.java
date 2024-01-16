package com.swisscom.mycoolservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
/**
 * Configuration class for creating a RestTemplate bean.
 * This configuration is used to define and provide a RestTemplate instance.
 */
@Configuration
public class RestTemplateConfig {
    /**
     * Creates and configures a RestTemplate bean.
     *
     * @return RestTemplate instance for making HTTP requests.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
