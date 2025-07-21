package com.test.zai.weather.service;

import java.util.Objects;

import org.springframework.web.client.RestClient;

import com.test.zai.weather.helper.ClientParser;
import com.test.zai.weather.model.WeatherDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WeatherClient {
    
    private final RestClient mainClient;
    private final RestClient backupClient;
    private final Integer maxRetry;
    private final String mainKey;
    private final String backupKey;

    public WeatherDTO getWeatherDetails(String city) {
        log.info("Calling Weather client...");
        WeatherDTO resp = null;

        for(int i = 0; i < maxRetry; i++) {
            try {
                resp = callMainAPI(city);
                if (Objects.nonNull(resp)) {
                    return resp;
                }
            } catch( Exception e) {
                log.error("Exception encountered when calling main API: ", e.getMessage());
            }
        }

        try {
            log.info("Trying Backup API...");
            resp = callBackupAPI();
            if (Objects.nonNull(resp)) {
                return resp;
            }
        } catch(Exception e) {
            log.error("Exception encountered when calling backup API: ", e.getMessage());
            throw new RuntimeException("Unable to retrieve weather data");
        }

        return resp;
    }

    private WeatherDTO callMainAPI(String city) {
        var msg = mainClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/current")
                .queryParam("access_key", mainKey)
                .queryParam("query", city)
                .build()
            ).retrieve()
            .body(String.class);

        log.info("Response Main API: {}", msg);
        return ClientParser.mainParse(msg);
    }

    private WeatherDTO callBackupAPI() {
        var msg = backupClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/weather")
                .queryParam("q", "melbourne,AU")
                .queryParam("appid", backupKey)
                .queryParam("units", "metric") // Celsius
                .build()
            ).retrieve()
            .body(String.class);

        log.info("Response Backup API: {}", msg);
        return ClientParser.backupParse(msg);
    }
}
