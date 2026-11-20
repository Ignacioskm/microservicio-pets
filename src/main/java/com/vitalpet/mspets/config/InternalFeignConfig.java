package com.vitalpet.mspets.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;

public class InternalFeignConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(
            @Value("${internal.api.key}") String apiKey) {

        return template ->
                template.header("X-Internal-Api-Key", apiKey);
    }
}

