package com.fomopotato.amityspace.service.legency;

import com.fomopotato.amityspace.data.record.LocationSensorMappingRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;


public class LocationSensorMappingDataService {

    private static final String CSV_PATH = "data/location_sensor_mapping.csv";

    private Map<Long, LocationSensorMappingRecord> cachedMappings;

    /**
     * Returns all location-to-sensor mapping records keyed by locationId.
     */
    public Map<Long, LocationSensorMappingRecord> getSensorMappingsByLocationId() {
        if (cachedMappings == null) {
            cachedMappings = loadMappingsFromCsv();
        }
        return cachedMappings;
    }

    /**
     * Returns one mapping record by locationId.
     */
    public LocationSensorMappingRecord getSensorMappingByLocationId(Long locationId) {
        if (locationId == null) {
            return null;
        }
        return getSensorMappingsByLocationId().get(locationId);
    }

    /**
     * Returns true when a mapping exists for the given locationId.
     */
    public boolean hasMappingForLocation(Long locationId) {
        return getSensorMappingByLocationId(locationId) != null;
    }

    private Map<Long, LocationSensorMappingRecord> loadMappingsFromCsv() {
        Map<Long, LocationSensorMappingRecord> mappings = new LinkedHashMap<>();

        try (InputStream inputStream = new ClassPathResource(CSV_PATH).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return mappings;
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(parseCsvLine(headerLine));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = parseCsvLine(line);

                Long locationId = getLong(columns, headerIndex, "location_id");
                if (locationId == null) {
                    continue;
                }

                String locationName = getText(columns, headerIndex, "location_name");
                Long crowdSensorLocationId = getLong(columns, headerIndex, "crowd_sensor_location_id");

                // Current record only has one field for noise sensor id/name.
                // Prefer noise_sensor_id, fallback to noise_sensor_name.
                String noiseSensorIdOrName = getText(columns, headerIndex, "noise_sensor_id");
                if (isBlank(noiseSensorIdOrName)) {
                    noiseSensorIdOrName = getText(columns, headerIndex, "noise_sensor_name");
                }

                Double noiseSensorLat = getDouble(columns, headerIndex, "noise_sensor_lat");
                Double noiseSensorLng = getDouble(columns, headerIndex, "noise_sensor_lng");
                String mappingMethod = getText(columns, headerIndex, "mapping_method");

                // Current record only has one confidence field.
                // Prefer noise_mapping_confidence, fallback to crowd_mapping_confidence.
                String mappingConfidence = getText(columns, headerIndex, "noise_mapping_confidence");
                if (isBlank(mappingConfidence)) {
                    mappingConfidence = getText(columns, headerIndex, "crowd_mapping_confidence");
                }

                String notes = getText(columns, headerIndex, "notes");

                LocationSensorMappingRecord record = new LocationSensorMappingRecord(
                        locationId,
                        locationName,
                        crowdSensorLocationId,
                        noiseSensorIdOrName,
                        noiseSensorLat,
                        noiseSensorLng,
                        mappingMethod,
                        mappingConfidence,
                        notes
                );

                // Keep first record if duplicate location_id appears.
                mappings.putIfAbsent(locationId, record);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load sensor mappings from CSV: " + CSV_PATH, e);
        }

        return mappings;
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(normalize(headers[i]), i);
        }
        return index;
    }

    private String getText(String[] columns, Map<String, Integer> headerIndex, String columnName) {
        Integer index = headerIndex.get(normalize(columnName));
        if (index == null || index < 0 || index >= columns.length) {
            return null;
        }

        String value = columns[index];
        if (value == null) {
            return null;
        }

        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private Long getLong(String[] columns, Map<String, Integer> headerIndex, String columnName) {
        String value = getText(columns, headerIndex, columnName);
        if (isBlank(value)) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getDouble(String[] columns, Map<String, Integer> headerIndex, String columnName) {
        String value = getText(columns, headerIndex, columnName);
        if (isBlank(value)) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV parser that supports quoted values.
        // Sufficient for current project CSV files.
        StringBuilder current = new StringBuilder();
        java.util.List<String> values = new java.util.ArrayList<>();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}