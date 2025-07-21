package com.test.zai.weather.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;



@TestConfiguration
@Profile("test")
public class MockClientConfig {
    
    @Bean
    public RestClient mainClient() {
        return mock(RestClient.class);
    }

    @Bean
    public RestClient backupClient() {
        return mock(RestClient.class);
    }
}
