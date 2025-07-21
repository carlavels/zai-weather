package com.test.zai.weather.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.test.zai.weather.service.WeatherClient;
import com.test.zai.weather.service.WeatherService;

@Configuration
public class ServiceConfiguration {

    @Bean
    public WeatherClient weatherClient(
        @Qualifier(value = "mainClient") RestClient mainClient,
        @Qualifier(value = "backupClient") RestClient backupClient,
        @Value("${app.api.retry}") Integer maxRetry,
        @Value("${app.api.main.api-key}") String mainKey,
        @Value("${app.api.backup.api-key}") String backupKey
    ) {
        return new WeatherClient(mainClient, backupClient, maxRetry, mainKey, backupKey);
    }

    @Bean
    public CacheManager cacheManager(
        @Value("${app.api.cache-time}") Integer maxCacheTime
    ) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("cityCache");
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
            .expireAfterWrite(maxCacheTime, TimeUnit.SECONDS)
            .maximumSize(1)
        );

        return cacheManager;
    }

    @Bean
    public WeatherService weatherService(
        WeatherClient client,
        @Value("${app.default-city}") String city
    ) {
        return new WeatherService(client, city);
    }
}
