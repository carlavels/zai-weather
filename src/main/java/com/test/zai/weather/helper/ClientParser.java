package com.test.zai.weather.helper;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.zai.weather.model.WeatherDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientParser {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static WeatherDTO mainParse(String msg) {
        try {
            Map<String, Object> jsonMsg = mapper.readValue(msg, Map.class);
            Map<String, Object> current = (Map) jsonMsg.get("current");
            var temp = current.get("temperature").toString();
            var windSpeed = current.get("wind_speed").toString();

            return WeatherDTO.builder()
                .temperatureDegrees(Integer.parseInt(temp))
                .windSpeed(Integer.parseInt(windSpeed))
                .build();
        } catch(Exception e) {
            log.error("Unable to parse response from main API: {}", e);
            throw new RuntimeException("Error parsing main API response.");
        }
    }

    public static WeatherDTO backupParse(String msg) {
        try {
            Map<String, Object> jsonMsg = mapper.readValue(msg, Map.class);

            Map<String, Object> main = (Map) jsonMsg.get("main");
            var temp = main.get("temp").toString();

            Map<String, Object> wind = (Map) jsonMsg.get("wind");
            var windSpeed = wind.get("speed").toString();

            return WeatherDTO.builder()
                .temperatureDegrees(Integer.parseInt(temp))
                .windSpeed(Integer.parseInt(windSpeed))
                .build();
        } catch(Exception e) {
            log.error("Unable to parse response from backup API: {}", e);
            throw new RuntimeException("Error parsing backup API response.");
        }
    }
}
