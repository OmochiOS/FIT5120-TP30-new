package com.fomopotato.amityspace.service.legency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fomopotato.amityspace.data.record.LocationHistoricalCrowdPatternRecord;
import com.fomopotato.amityspace.data.record.LocationProfileStaticRecord;
import com.fomopotato.amityspace.data.record.LocationSensorMappingRecord;
import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;
import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;
import com.fomopotato.amityspace.model.Location;
import com.fomopotato.amityspace.model.SensoryProfile;
import com.fomopotato.amityspace.service.calculator.CrowdLevelCalculator;
import com.fomopotato.amityspace.service.calculator.LightingCalculator;
import com.fomopotato.amityspace.service.calculator.NoiseCalculator;
import com.fomopotato.amityspace.service.calculator.PredictabilityCalculator;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocationDataService {

    private final LocationApiService locationApiService;
    private final LocationProfileStaticDataService locationProfileStaticDataService;
    private final LocationSensorMappingDataService locationSensorMappingDataService;
    private final LocationHistoricalCrowdPatternDataService locationHistoricalCrowdPatternDataService;
    private final CrowdLevelCalculator crowdLevelCalculator;
    private final PredictabilityCalculator predictabilityCalculator;
    private final NoiseCalculator noiseCalculator;
    private final LightingApiService lightingApiService;
    private final LightingCalculator lightingCalculator;

    public LocationDataService(LocationApiService locationApiService,
                               LocationProfileStaticDataService locationProfileStaticDataService,
                               LocationSensorMappingDataService locationSensorMappingDataService,
                               LocationHistoricalCrowdPatternDataService locationHistoricalCrowdPatternDataService,
                               CrowdLevelCalculator crowdLevelCalculator,
                               PredictabilityCalculator predictabilityCalculator,
                               NoiseCalculator noiseCalculator,
                               LightingApiService lightingApiService,
                               LightingCalculator lightingCalculator) {
        this.locationApiService = locationApiService;
        this.locationProfileStaticDataService = locationProfileStaticDataService;
        this.locationSensorMappingDataService = locationSensorMappingDataService;
        this.locationHistoricalCrowdPatternDataService = locationHistoricalCrowdPatternDataService;
        this.crowdLevelCalculator = crowdLevelCalculator;
        this.predictabilityCalculator = predictabilityCalculator;
        this.noiseCalculator = noiseCalculator;
        this.lightingApiService = lightingApiService;
        this.lightingCalculator = lightingCalculator;
    }

    public List<Location> getLocations() {
        List<LocationApiResponse> apiLocations = locationApiService.fetchLocationRecords();
        List<CrowdApiResponse> latestCrowd = locationApiService.fetchLatestCrowdRecords();
        List<CrowdApiResponse> historicalCrowd = locationApiService.fetchHistoricalCrowdRecords();
        List<NoiseApiResponse> noiseRecords = locationApiService.fetchNoiseRecords();

        Map<String, LocationProfileStaticRecord> staticProfiles = locationProfileStaticDataService.getProfileRecordsByName();
        Map<Long, LocationSensorMappingRecord> sensorMappings = locationSensorMappingDataService.getSensorMappingsByLocationId();
        Map<Long, List<LocationHistoricalCrowdPatternRecord>> historicalPatterns = locationHistoricalCrowdPatternDataService.getHistoricalPatternsByLocationId();
        Map<Long, Integer> latestCrowdMap = buildLatestCrowdMap(latestCrowd);
        Map<Long, List<Integer>> crowdSeriesMap = buildCrowdSeriesMap(latestCrowd, historicalCrowd);
        List<Location> locations = new ArrayList<>();

        for (LocationApiResponse apiLocation : apiLocations) {
            Location location = new Location();
            location.setId(apiLocation.getLocationId());
            location.setName(apiLocation.getName());
            location.setLatitude(apiLocation.getLatitude());
            location.setLongitude(apiLocation.getLongitude());

            LocationProfileStaticRecord staticRecord = staticProfiles.get(normalize(apiLocation.getName()));
            location.setPlaceType(staticRecord != null ? mapPlaceType(staticRecord.getPlaceType()) : inferPlaceType(location));

            CrowdLevel crowdLevel = crowdLevelCalculator.resolveCrowdLevel(latestCrowdMap.get(apiLocation.getLocationId()));
            location.setCrowdLevel(crowdLevel);
            location.setStatusLabel(crowdLevelCalculator.buildStatusLabel(crowdLevel));

            SensoryProfile profile = buildSensoryProfile(
                    location,
                    staticRecord,
                    sensorMappings.get(apiLocation.getLocationId()),
                    historicalPatterns.get(apiLocation.getLocationId()),
                    crowdSeriesMap.get(apiLocation.getLocationId()),
                    noiseRecords
            );

            location.setSensoryProfile(profile);
            locations.add(location);
        }

        return locations;
    }

    private SensoryProfile buildSensoryProfile(Location location,
                                               LocationProfileStaticRecord staticRecord,
                                               LocationSensorMappingRecord mappingRecord,
                                               List<LocationHistoricalCrowdPatternRecord> historicalPatternRecords,
                                               List<Integer> crowdSeries,
                                               List<NoiseApiResponse> noiseRecords) {
        SensoryProfile profile = new SensoryProfile();
        profile.setLocationId(location.getId());

        profile.setLightingLevel(resolveLightingLevel(location, staticRecord));

        if (staticRecord != null) {
            profile.setQuietSpaceAvailable(Boolean.TRUE.equals(staticRecord.getQuietSpaceAvailable()));
            profile.setOverallSensoryScore(defaultText(staticRecord.getOverallSensoryScore(), "Data unavailable"));
            profile.setBestTimesToVisit(defaultText(staticRecord.getBestTimesToVisit(), "Data unavailable"));
            profile.setGoodToKnowTips(defaultText(staticRecord.getGoodToKnowTips(), "Data unavailable"));
            profile.setSummary(defaultText(staticRecord.getSummary(), "Sensory profile data is currently unavailable."));
        } else {
            profile.setQuietSpaceAvailable(false);
            profile.setOverallSensoryScore("Data unavailable");
            profile.setBestTimesToVisit("Data unavailable");
            profile.setGoodToKnowTips("Data unavailable");
            profile.setSummary("Sensory profile data is currently unavailable.");
        }

        Double noiseDb = noiseCalculator.resolveNoiseDb(location, mappingRecord, noiseRecords);
        profile.setNoiseDb(noiseDb);
        profile.setNoiseLevel(noiseCalculator.mapNoiseLevel(noiseDb));

        if (historicalPatternRecords != null && !historicalPatternRecords.isEmpty()) {
            String bestTime = extractRecommendedBestTime(historicalPatternRecords);
            if (bestTime != null) {
                profile.setBestTimesToVisit(bestTime);
            }

            String predictability = extractPredictabilityLevel(historicalPatternRecords);
            if (predictability != null) {
                profile.setPredictabilityLevel(mapPredictabilityLevel(predictability));
            } else {
                profile.setPredictabilityLevel(predictabilityCalculator.resolvePredictability(crowdSeries));
            }
        } else {
            profile.setPredictabilityLevel(predictabilityCalculator.resolvePredictability(crowdSeries));
        }

        profile.setLastUpdated(resolveLastUpdated(staticRecord, historicalPatternRecords));

        return profile;
    }

    private SensoryLevel resolveLightingLevel(Location location, LocationProfileStaticRecord staticRecord) {
        SensoryLevel staticLighting = mapLightingLevel(staticRecord != null ? staticRecord.getLightingLevel() : null);

        if (isOutdoorLocation(location)) {
            try {
                JsonNode weatherData = lightingApiService.fetchCurrentWeather(location.getLatitude(), location.getLongitude());
                Double lux = lightingCalculator.estimateLux(location.getLatitude(), location.getLongitude(), weatherData);
                SensoryLevel apiLighting = lightingCalculator.mapLightingLevel(lux);
                if (apiLighting != null) {
                    return apiLighting;
                }
            } catch (Exception e) {
                // Fall back to static lighting below.
            }
        }

        return staticLighting;
    }

    private boolean isOutdoorLocation(Location location) {
        if (location == null) {
            return false;
        }

        if (location.getPlaceType() == PlaceType.OUTDOOR) {
            return true;
        }

        String name = location.getName();
        if (name == null) {
            return false;
        }

        String normalized = name.trim().toLowerCase();

        return normalized.contains("bridge")
                || normalized.contains("park")
                || normalized.contains("garden")
                || normalized.contains("street")
                || normalized.contains("st ")
                || normalized.contains("footpath")
                || normalized.contains("walk")
                || normalized.contains("square")
                || normalized.contains("point")
                || normalized.contains("esplanade");
    }

    private Map<Long, Integer> buildLatestCrowdMap(List<CrowdApiResponse> crowdRecords) {
        Map<Long, Integer> map = new HashMap<>();
        for (CrowdApiResponse record : crowdRecords) {
            map.putIfAbsent(record.getLocationId(), record.getTotalCount());
        }
        return map;
    }

    private Map<Long, List<Integer>> buildCrowdSeriesMap(List<CrowdApiResponse> latest, List<CrowdApiResponse> historical) {
        Map<Long, List<Integer>> map = new HashMap<>();

        for (CrowdApiResponse record : latest) {
            map.computeIfAbsent(record.getLocationId(), k -> new ArrayList<>());
            if (map.get(record.getLocationId()).size() < 12) {
                map.get(record.getLocationId()).add(record.getTotalCount());
            }
        }

        for (CrowdApiResponse record : historical) {
            map.computeIfAbsent(record.getLocationId(), k -> new ArrayList<>());
            if (map.get(record.getLocationId()).size() < 24) {
                map.get(record.getLocationId()).add(record.getTotalCount());
            }
        }

        return map;
    }

    private String extractRecommendedBestTime(List<LocationHistoricalCrowdPatternRecord> records) {
        for (LocationHistoricalCrowdPatternRecord record : records) {
            if (Boolean.TRUE.equals(record.getRecommendedVisitFlag())) {
                String timeBlock = record.getTimeBlock();
                if (timeBlock != null && !timeBlock.isBlank()) {
                    return "Weekday mornings (" + timeBlock + ")";
                }
            }
        }
        return null;
    }

    private String extractPredictabilityLevel(List<LocationHistoricalCrowdPatternRecord> records) {
        for (LocationHistoricalCrowdPatternRecord record : records) {
            if (record.getPredictabilityLevel() != null && !record.getPredictabilityLevel().isBlank()) {
                return record.getPredictabilityLevel();
            }
        }
        return null;
    }

    private String resolveLastUpdated(LocationProfileStaticRecord staticRecord,
                                      List<LocationHistoricalCrowdPatternRecord> historicalPatternRecords) {
        if (staticRecord != null && staticRecord.getLastReviewed() != null && !staticRecord.getLastReviewed().isBlank()) {
            return staticRecord.getLastReviewed();
        }

        String historicalUpdatedAt = extractHistoricalUpdatedAt(historicalPatternRecords);
        if (historicalUpdatedAt != null) {
            return historicalUpdatedAt;
        }

        return "Data update time unavailable";
    }

    private String extractHistoricalUpdatedAt(List<LocationHistoricalCrowdPatternRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }

        for (LocationHistoricalCrowdPatternRecord record : records) {
            if (record.getUpdatedAt() != null && !record.getUpdatedAt().isBlank()) {
                return record.getUpdatedAt();
            }
        }

        return null;
    }

    private PredictabilityLevel mapPredictabilityLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }

        switch (level.trim().toUpperCase()) {
            case "HIGH":
                return PredictabilityLevel.HIGH;
            case "MODERATE":
                return PredictabilityLevel.MODERATE;
            case "LOW":
                return PredictabilityLevel.LOW;
            default:
                return null;
        }
    }

    private SensoryLevel mapLightingLevel(String lightingLevel) {
        if (lightingLevel == null || lightingLevel.isBlank()) {
            return null;
        }

        switch (lightingLevel.trim().toUpperCase()) {
            case "LOW":
            case "CALM":
            case "NATURAL":
                return SensoryLevel.LOW;

            case "MODERATE":
            case "MIXED":
            case "MIXED-NATURAL":
            case "MIXED / NATURAL":
                return SensoryLevel.MODERATE;

            case "HIGH":
            case "BUSY":
            case "HARSH":
            case "ARTIFICIAL":
                return SensoryLevel.HIGH;

            case "OVERSTIMULATING":
                return SensoryLevel.OVERSTIMULATING;

            default:
                return null;
        }
    }

    private PlaceType mapPlaceType(String placeType) {
        if (placeType == null || placeType.isBlank()) {
            return null;
        }

        switch (placeType.trim().toUpperCase()) {
            case "OUTDOOR":
            case "PARK":
            case "GARDEN":
            case "PLAYGROUND":
            case "TRAIL":
                return PlaceType.OUTDOOR;

            case "CULTURAL":
            case "LIBRARY":
            case "MUSEUM":
            case "GALLERY":
            case "THEATRE":
                return PlaceType.CULTURAL;

            case "SOCIAL":
            case "MARKET":
            case "PRECINCT":
            case "PLAZA":
                return PlaceType.SOCIAL;

            case "ACTIVE":
            case "SPORT":
            case "RECREATION":
            case "GYM":
                return PlaceType.ACTIVE;

            default:
                return null;
        }
    }

    private PlaceType inferPlaceType(Location location) {
        if (location == null || location.getName() == null) {
            return null;
        }

        String normalized = normalize(location.getName());

        if (normalized.contains("park") || normalized.contains("garden") || normalized.contains("square")) {
            return PlaceType.OUTDOOR;
        }

        if (normalized.contains("library") || normalized.contains("museum") || normalized.contains("gallery") || normalized.contains("theatre")) {
            return PlaceType.CULTURAL;
        }

        return null;
    }

    private String defaultText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }
}
