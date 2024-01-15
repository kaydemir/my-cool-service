package com.swisscom.mycoolservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.swisscom.mycoolservice.properties.ApplicationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class MyCoolServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyCoolServiceApplication.class, args);
	}

}
