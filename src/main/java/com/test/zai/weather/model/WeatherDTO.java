package com.test.zai.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherDTO {
    
    @JsonProperty(value = "wind_speed")
    private String windSpeed;

    @JsonProperty(value = "temperature_degrees")
    private String temperatureDegrees;
}
