package com.test.zai.weather.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.test.zai.weather.model.WeatherDTO;
import com.test.zai.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WeatherController {
    
    private final WeatherService service;

    @GetMapping("/v1/weather")
    public ResponseEntity<WeatherDTO> getWeatherByCity(
        @RequestParam(value = "city", required = false) String city
    ) {

        log.info("City: {}", city);
        var resp = service.getWeatherDetails(city);
        return ResponseEntity.ok().body(resp);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleExceptions(RuntimeException e) {
        return ResponseEntity.internalServerError()
            .body(Map.of("message", e.getMessage()));
    }
}
