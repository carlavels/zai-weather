package com.test.zai.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("!test")
public class ClientConfiguration {

    @Bean
    public RestClient mainClient(
        RestClient.Builder builder,
        @Value("${app.api.main.url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean
    public RestClient backupClient(
        RestClient.Builder builder,
        @Value("${app.api.backup.url}") String url) {
        return builder.baseUrl(url).build();
    }
}
