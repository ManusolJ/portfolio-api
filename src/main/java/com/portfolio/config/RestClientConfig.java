package com.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

/** Supplies the shared RestClient; timeouts come from `spring.http.clients.*`. */
@Configuration
public class RestClientConfig {

    @Bean
    RestClient restClient(Builder builder) {
        return builder.build();
    }
}
