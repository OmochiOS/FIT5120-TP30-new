package com.fomopotato.amityspace.service.calculator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fomopotato.amityspace.enums.SensoryLevel;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class LightingCalculator {

    public Double estimateLux(double lat, double lon, JsonNode weatherData) {
        if (weatherData == null || !weatherData.has("current")) {
            return null;
        }

        JsonNode current = weatherData.get("current");

        long timestamp = current.path("dt").asLong(0);
        int timezoneOffset = weatherData.path("timezone_offset").asInt(0);
        int cloudPercent = current.path("clouds").asInt(50);
        double visibilityKm = current.path("visibility").asDouble(10000) / 1000.0;

        JsonNode weather = current.path("weather").isArray() && current.path("weather").size() > 0
                ? current.path("weather").get(0)
                : null;

        int weatherId = weather != null ? weather.path("id").asInt(800) : 800;
        String weatherIcon = weather != null ? weather.path("icon").asText("01d") : "01d";

        double solarElevation = calculateSolarElevation(lat, lon, timestamp, timezoneOffset);
        return estimateLuxFromInputs(solarElevation, cloudPercent, weatherId, weatherIcon, visibilityKm);
    }

    public SensoryLevel mapLightingLevel(Double lux) {
        if (lux == null) {
            return null;
        }

        if (lux < 1000) {
            return SensoryLevel.LOW;
        }

        if (lux < 10000) {
            return SensoryLevel.MODERATE;
        }

        return SensoryLevel.HIGH;
    }

    private double calculateSolarElevation(double lat, double lon, long timestamp, int timezoneOffset) {
        ZonedDateTime local = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneOffset.UTC)
                .plusSeconds(timezoneOffset);

        int dayOfYear = local.getDayOfYear();
        double hour = local.getHour() + local.getMinute() / 60.0;

        double declination = 23.45 * Math.sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)));
        double hourAngle = 15.0 * (hour - 12.0);

        double latRad = Math.toRadians(lat);
        double decRad = Math.toRadians(declination);
        double haRad = Math.toRadians(hourAngle);

        double sinAlt = Math.sin(latRad) * Math.sin(decRad)
                + Math.cos(latRad) * Math.cos(decRad) * Math.cos(haRad);

        return Math.toDegrees(Math.asin(sinAlt));
    }

    private double estimateLuxFromInputs(double solarElevation,
                                         int cloudPercent,
                                         int weatherId,
                                         String weatherIcon,
                                         double visibilityKm) {
        if (solarElevation <= -6) {
            return 0;
        }

        double maxLuxClear = 120000.0;

        double sunFactor;
        if (solarElevation > 0) {
            double airMass = 1.0 / Math.max(0.01, Math.sin(Math.toRadians(solarElevation)));
            sunFactor = Math.sin(Math.toRadians(solarElevation)) * Math.exp(-0.14 * (airMass - 1));
        } else {
            sunFactor = 0.08 * (solarElevation + 6) / 6.0;
        }

        double weatherFactor = getWeatherFactor(weatherId, cloudPercent, weatherIcon);

        double visibilityFactor;
        if (visibilityKm < 5) {
            visibilityFactor = 0.3 + 0.14 * visibilityKm;
        } else if (visibilityKm < 20) {
            visibilityFactor = 0.9 + 0.02 * (visibilityKm - 5);
        } else {
            visibilityFactor = 1.0;
        }

        double lux = maxLuxClear * sunFactor * weatherFactor * visibilityFactor;
        return Math.max(0, Math.min(lux, 130000));
    }

    private double getWeatherFactor(int weatherId, int cloudsPercent, String icon) {
        if (icon != null && icon.endsWith("n")) {
            return 0.02;
        }

        if (weatherId == 800) {
            return 1.0;
        }

        if (weatherId >= 801 && weatherId <= 804) {
            if (cloudsPercent <= 40) {
                return clamp(1.0 - 0.004 * cloudsPercent, 0.25, 0.96);
            } else if (cloudsPercent <= 75) {
                return clamp(0.84 - 0.008 * (cloudsPercent - 40), 0.25, 0.96);
            } else {
                return clamp(0.28 + 0.02 * Math.exp(-0.04 * (cloudsPercent - 75)), 0.25, 0.96);
            }
        }

        if (weatherId >= 200 && weatherId < 300) {
            return 0.15;
        }

        if (weatherId >= 500 && weatherId < 600) {
            return 0.35;
        }

        if (weatherId >= 600 && weatherId < 700) {
            return 0.40;
        }

        if (weatherId >= 700 && weatherId < 800) {
            return 0.45;
        }

        if (weatherId >= 300 && weatherId < 400) {
            return 0.45;
        }

        return 0.50;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}