package com.fomopotato.amityspace.service.legency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


public class LightingApiService {

    private static final String OPENWEATHER_URL =
            "https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&exclude=minutely,hourly,daily,alerts&appid=%s&units=metric";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openweather.api.key}")
    private String apiKey;

    public LightingApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode fetchCurrentWeather(double lat, double lon) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing OpenWeather API key");
        }

        String url = String.format(OPENWEATHER_URL, lat, lon, apiKey);

        try {
            String body = restTemplate.getForObject(url, String.class);
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("Empty response from OpenWeather API");
            }
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch lighting/weather data from OpenWeather", e);
        }
    }
}