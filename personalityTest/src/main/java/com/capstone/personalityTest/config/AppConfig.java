package com.capstone.personalityTest.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;
import jakarta.servlet.MultipartConfigElement;

/**
 * General application configuration.
 * Enables asynchronous processing and defines shared beans.
 */
@Configuration
@EnableAsync
public class AppConfig {

    /**
     * RestTemplate bean for making REST API calls to the Python AI service.
     * Used by AIIntegrationService and CareerDocumentService.
     * 
     * @return a new RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Value("${spring.servlet.multipart.max-file-size:NOT_SET}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:NOT_SET}")
    private String maxRequestSize;

    @PostConstruct
    public void printConfig() {
        log.info("🔍 [DEBUG] Multipart Config - Max File Size: {}", maxFileSize);
        log.info("🔍 [DEBUG] Multipart Config - Max Request Size: {}", maxRequestSize);
    }
}
