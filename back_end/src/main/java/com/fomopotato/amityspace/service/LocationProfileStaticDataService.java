package com.fomopotato.amityspace.service;

import com.fomopotato.amityspace.data.record.LocationProfileStaticRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LocationProfileStaticDataService {

    private static final String CSV_PATH = "data/location_profile_static.csv";

    private Map<String, LocationProfileStaticRecord> cachedRecordsByName;

    public Map<String, LocationProfileStaticRecord> getProfileRecordsByName() {
        if (cachedRecordsByName == null) {
            cachedRecordsByName = loadProfileRecordsFromCsv();
        }
        return cachedRecordsByName;
    }

    public LocationProfileStaticRecord getProfileRecordByName(String locationName) {
        return getProfileRecordsByName().get(normalize(locationName));
    }

    private Map<String, LocationProfileStaticRecord> loadProfileRecordsFromCsv() {
        Map<String, LocationProfileStaticRecord> records = new LinkedHashMap<>();

        try (InputStream inputStream = new ClassPathResource(CSV_PATH).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return records;
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(parseCsvLine(headerLine));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = parseCsvLine(line);

                Long locationId = getLong(columns, headerIndex, "id");
                String locationName = getText(columns, headerIndex, "name");

                if (locationName == null || locationName.isBlank()) {
                    continue;
                }

                String placeType = getText(columns, headerIndex, "place_type");
                Boolean quietSpaceAvailable = getBoolean(columns, headerIndex, "quiet_space_available");
                String overallSensoryScore = getText(columns, headerIndex, "overall_sensory_score");
                String goodToKnowTips = getText(columns, headerIndex, "good_to_know_tips");
                String lastReviewed = getText(columns, headerIndex, "last_reviewed");

                // lighting is now dynamic, so do not depend on CSV for it
                String lightingLevel = null;

                // current CSV does not have these as stable fields
                String summary = null;
                String escapeRouteNotes = null;
                String bestTimesToVisit = null;
                String confidence = null;

                String source = defaultText(
                        getText(columns, headerIndex, "data_source"),
                        "DS location_profile_static"
                );

                LocationProfileStaticRecord record = new LocationProfileStaticRecord(
                        locationId,
                        locationName,
                        placeType,
                        lightingLevel,
                        quietSpaceAvailable,
                        overallSensoryScore,
                        summary,
                        goodToKnowTips,
                        escapeRouteNotes,
                        bestTimesToVisit,
                        lastReviewed,
                        source,
                        confidence
                );

                records.putIfAbsent(normalize(locationName), record);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load location profile static CSV: " + CSV_PATH, e);
        }

        return records;
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
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean getBoolean(String[] columns, Map<String, Integer> headerIndex, String columnName) {
        String value = getText(columns, headerIndex, columnName);
        if (value == null || value.isBlank()) {
            return null;
        }

        if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "0".equals(value)) {
            return false;
        }

        return null;
    }

    private String defaultText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String[] parseCsvLine(String line) {
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

    private String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }
}