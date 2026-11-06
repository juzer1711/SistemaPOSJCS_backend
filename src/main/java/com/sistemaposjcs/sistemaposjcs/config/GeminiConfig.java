package com.sistemaposjcs.sistemaposjcs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {

    @Bean
    public RestClient geminiRestClient(
            GeminiProperties geminiProperties
    ) {
        return RestClient.builder()
                .baseUrl(geminiProperties.getBaseUrl())
                .build();
    }
}
