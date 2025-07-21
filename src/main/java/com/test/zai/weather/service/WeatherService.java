package com.test.zai.weather.service;

import java.util.Objects;

import org.springframework.cache.annotation.Cacheable;

import com.test.zai.weather.model.WeatherDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WeatherService {
    
    private final WeatherClient weatherClient;
    private final String defaultCity;


    @Cacheable("cityCache")
    public WeatherDTO getWeatherDetails(String city) {
        var cty = Objects.isNull(city) ? defaultCity:city;

        log.info("Getting details for city: {}", cty);
        return weatherClient.getWeatherDetails(cty);
    }
}
