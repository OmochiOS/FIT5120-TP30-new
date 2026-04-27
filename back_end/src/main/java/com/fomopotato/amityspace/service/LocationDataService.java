package com.fomopotato.amityspace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fomopotato.amityspace.data.record.LocationHistoricalCrowdPatternRecord;
import com.fomopotato.amityspace.data.record.LocationProfileStaticRecord;
import com.fomopotato.amityspace.data.record.LocationSensorMappingRecord;
import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;
import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;
import com.fomopotato.amityspace.integration.dto.CrowdApiResponse;
import com.fomopotato.amityspace.integration.dto.LocationApiResponse;
import com.fomopotato.amityspace.integration.dto.NoiseApiResponse;
import com.fomopotato.amityspace.model.Location;
import com.fomopotato.amityspace.model.SensoryProfile;
import com.fomopotato.amityspace.service.calculator.CrowdLevelCalculator;
import com.fomopotato.amityspace.service.calculator.LightingCalculator;
import com.fomopotato.amityspace.service.calculator.NoiseCalculator;
import com.fomopotato.amityspace.service.calculator.PredictabilityCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationDataService {

    private static final String DEFAULT_OVERALL_SCORE = "Moderate";
    private static final String DEFAULT_BEST_TIMES = "Weekday mornings are usually the calmest starting point.";
    private static final String DEFAULT_GOOD_TO_KNOW = "Use quieter edges where possible. | Keep a simple exit route in mind. | Plan a short first visit before extending your stay.";
    private static final String DEFAULT_SUMMARY = "Current comfort guidance is estimated from crowd, noise, lighting and place context.";

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

        Map<Long, Location> dedupedLocations = new LinkedHashMap<>();

        for (LocationApiResponse apiLocation : apiLocations) {
            Long locationId = apiLocation.getLocationId();
            if (locationId == null || dedupedLocations.containsKey(locationId)) {
                continue;
            }

            Location location = new Location();
            location.setId(locationId);
            location.setName(apiLocation.getName());
            location.setLatitude(apiLocation.getLatitude());
            location.setLongitude(apiLocation.getLongitude());

            LocationProfileStaticRecord staticRecord = staticProfiles.get(normalize(apiLocation.getName()));
            PlaceType mappedPlaceType = staticRecord != null ? mapPlaceType(staticRecord.getPlaceType()) : null;
            location.setPlaceType(mappedPlaceType != null ? mappedPlaceType : inferPlaceType(location));

            CrowdLevel crowdLevel = crowdLevelCalculator.resolveCrowdLevel(latestCrowdMap.get(locationId));
            location.setCrowdLevel(crowdLevel);
            location.setStatusLabel(crowdLevelCalculator.buildStatusLabel(crowdLevel));

            SensoryProfile profile = buildSensoryProfile(
                    location,
                    staticRecord,
                    sensorMappings.get(locationId),
                    historicalPatterns.get(locationId),
                    crowdSeriesMap.get(locationId),
                    noiseRecords
            );

            location.setSensoryProfile(profile);
            dedupedLocations.put(locationId, location);
        }

        List<Location> locations = new ArrayList<>(dedupedLocations.values());
        locations.sort(Comparator.comparing(Location::getId));
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

        profile.setLightingLevel(resolveLightingLevel(location));

        if (staticRecord != null) {
            profile.setQuietSpaceAvailable(Boolean.TRUE.equals(staticRecord.getQuietSpaceAvailable()));
            profile.setOverallSensoryScore(defaultText(staticRecord.getOverallSensoryScore(), DEFAULT_OVERALL_SCORE));
            profile.setBestTimesToVisit(defaultText(staticRecord.getBestTimesToVisit(), DEFAULT_BEST_TIMES));
            profile.setGoodToKnowTips(defaultText(staticRecord.getGoodToKnowTips(), DEFAULT_GOOD_TO_KNOW));
            profile.setSummary(defaultText(staticRecord.getSummary(), DEFAULT_SUMMARY));
        } else {
            profile.setQuietSpaceAvailable(false);
            profile.setOverallSensoryScore(DEFAULT_OVERALL_SCORE);
            profile.setBestTimesToVisit(DEFAULT_BEST_TIMES);
            profile.setGoodToKnowTips(DEFAULT_GOOD_TO_KNOW);
            profile.setSummary(DEFAULT_SUMMARY);
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

    private SensoryLevel resolveLightingLevel(Location location) {
        if (!isOutdoorLocation(location)) {
            return null;
        }

        try {
            JsonNode weatherData = lightingApiService.fetchCurrentWeather(location.getLatitude(), location.getLongitude());
            Double lux = lightingCalculator.estimateLux(location.getLatitude(), location.getLongitude(), weatherData);
            return lightingCalculator.mapLightingLevel(lux);
        } catch (Exception e) {
            return null;
        }
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

        return LocalDate.now().toString();
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

    private PlaceType mapPlaceType(String placeType) {
        if (placeType == null || placeType.isBlank()) {
            return null;
        }

        String normalized = placeType.trim().toUpperCase();

        if (normalized.contains("OUTDOOR")
                || normalized.contains("PARK")
                || normalized.contains("GARDEN")
                || normalized.contains("PLAYGROUND")
                || normalized.contains("TRAIL")) {
            return PlaceType.OUTDOOR;
        }

        if (normalized.contains("CULTURAL")
                || normalized.contains("LIBRARY")
                || normalized.contains("MUSEUM")
                || normalized.contains("GALLERY")
                || normalized.contains("THEATRE")) {
            return PlaceType.CULTURAL;
        }

        if (normalized.contains("SOCIAL")
                || normalized.contains("MARKET")
                || normalized.contains("PRECINCT")
                || normalized.contains("PLAZA")) {
            return PlaceType.SOCIAL;
        }

        if (normalized.contains("ACTIVE")
                || normalized.contains("SPORT")
                || normalized.contains("RECREATION")
                || normalized.contains("GYM")) {
            return PlaceType.ACTIVE;
        }

        return null;
    }

    private PlaceType inferPlaceType(Location location) {
        if (location == null || location.getName() == null) {
            return PlaceType.ANY;
        }

        String normalized = normalize(location.getName());

        if (normalized.contains("park")
                || normalized.contains("garden")
                || normalized.contains("square")
                || normalized.contains("bridge")
                || normalized.contains("street")
                || normalized.contains("st ")
                || normalized.contains("esplanade")
                || normalized.contains("point")) {
            return PlaceType.OUTDOOR;
        }

        if (normalized.contains("library")
                || normalized.contains("museum")
                || normalized.contains("gallery")
                || normalized.contains("theatre")) {
            return PlaceType.CULTURAL;
        }

        if (normalized.contains("community")
                || normalized.contains("hub")
                || normalized.contains("market")) {
            return PlaceType.SOCIAL;
        }

        if (normalized.contains("bath")
                || normalized.contains("sport")
                || normalized.contains("gym")) {
            return PlaceType.ACTIVE;
        }

        return PlaceType.ANY;
    }

    private String defaultText(String value, String fallback) {
        return (value == null || value.isBlank() || isPlaceholderText(value)) ? fallback : value;
    }

    private boolean isPlaceholderText(String value) {
        String normalized = value.trim().toLowerCase();

        return normalized.equals("data unavailable")
                || normalized.contains("currently unavailable")
                || normalized.contains("not available")
                || normalized.contains("unavailable");
    }

    private String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }
}
