package com.fomopotato.amityspace.service.legency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fomopotato.amityspace.integration.dto.CrowdApiResponse;
import com.fomopotato.amityspace.integration.dto.LocationApiResponse;
import com.fomopotato.amityspace.integration.dto.NoiseApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


public class LocationApiService {

    private static final String SENSOR_LOCATIONS_URL =
            "https://melbournetestbed.opendatasoft.com/api/explore/v2.1/catalog/datasets/pedestrian-counting-system-sensor-locations/records?limit=-1";

    private static final String CROWD_COUNTS_URL =
            "https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/pedestrian-counting-system-past-hour-counts-per-minute/records?limit=100";

    private static final String HISTORICAL_CROWD_URL =
            "https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/pedestrian-counting-system-monthly-counts-per-hour/records?limit=100";

    private static final String MICROCLIMATE_URL =
            "https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/microclimate-sensors-data/records?limit=100";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LocationApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<LocationApiResponse> fetchLocationRecords() {
        JsonNode root = getJson(SENSOR_LOCATIONS_URL);
        List<LocationApiResponse> results = new ArrayList<>();

        for (JsonNode row : getResults(root)) {
            Long id = getLong(row, "location_id", "Location_ID");
            Double lat = getDouble(row, "latitude", "Latitude");
            Double lng = getDouble(row, "longitude", "Longitude");
            String name = getText(row, "sensor_description", "location_description", "Location");

            if (id != null && lat != null && lng != null && name != null && !name.isBlank()) {
                results.add(new LocationApiResponse(id, name, lat, lng));
            }
        }

        return results;
    }

    public List<CrowdApiResponse> fetchLatestCrowdRecords() {
        JsonNode root = getJson(CROWD_COUNTS_URL);
        List<CrowdApiResponse> results = new ArrayList<>();

        for (JsonNode row : getResults(root)) {
            Long locationId = getLong(row, "location_id", "Location_ID");
            Integer total = getInt(row, "total_of_directions", "Total_of_Directions");

            if (locationId != null && total != null) {
                results.add(new CrowdApiResponse(locationId, total));
            }
        }

        return results;
    }

    public List<CrowdApiResponse> fetchHistoricalCrowdRecords() {
        JsonNode root = getJson(HISTORICAL_CROWD_URL);
        List<CrowdApiResponse> results = new ArrayList<>();

        for (JsonNode row : getResults(root)) {
            Long locationId = getLong(row, "location_id", "Location_ID");
            Integer total = getInt(row, "total_of_directions", "Total_of_Directions");

            if (locationId != null && total != null) {
                results.add(new CrowdApiResponse(locationId, total));
            }
        }

        return results;
    }

    public List<NoiseApiResponse> fetchNoiseRecords() {
        JsonNode root = getJson(MICROCLIMATE_URL);
        List<NoiseApiResponse> results = new ArrayList<>();

        for (JsonNode row : getResults(root)) {
            Double noise = getDouble(row, "noise", "Noise");
            Double lat = null;
            Double lng = null;

            // 1) try flat fields first
            lat = getDouble(row, "lat", "Lat", "latitude", "Latitude");
            lng = getDouble(row, "lng", "Lng", "lon", "Lon", "longitude", "Longitude");

            // 2) fallback: nested latlong object
            if (lat == null || lng == null) {
                JsonNode latLongNode = row.get("latlong");
                if (latLongNode != null && latLongNode.isObject()) {
                    if (latLongNode.hasNonNull("lat")) {
                        lat = latLongNode.get("lat").asDouble();
                    }
                    if (latLongNode.hasNonNull("lon")) {
                        lng = latLongNode.get("lon").asDouble();
                    }
                }
            }

            if (noise != null && lat != null && lng != null) {
                results.add(new NoiseApiResponse(lat, lng, noise));
            }
        }

        return results;
    }
    private JsonNode getJson(String url) {
        try {
            String body = restTemplate.getForObject(url, String.class);
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("Empty response from API: " + url);
            }
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch API data from: " + url, e);
        }
    }

    private Iterable<JsonNode> getResults(JsonNode root) {
        JsonNode results = root.get("results");
        if (results == null || !results.isArray()) {
            return List.of();
        }
        return results;
    }

    private String getText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.hasNonNull(key)) {
                return node.get(key).asText();
            }
        }
        return null;
    }

    private Long getLong(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.hasNonNull(key)) {
                return node.get(key).asLong();
            }
        }
        return null;
    }

    private Integer getInt(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.hasNonNull(key)) {
                return node.get(key).asInt();
            }
        }
        return null;
    }

    private Double getDouble(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.hasNonNull(key)) {
                return node.get(key).asDouble();
            }
        }
        return null;
    }
}