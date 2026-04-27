package com.fomopotato.amityspace.service.legency;

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

                Long locationId = firstNonNull(
                        getLong(columns, headerIndex, "location_id"),
                        getLong(columns, headerIndex, "id")
                );

                String locationName = firstNonBlank(
                        getText(columns, headerIndex, "location_name"),
                        getText(columns, headerIndex, "name")
                );

                if (locationName == null || locationName.isBlank()) {
                    continue;
                }

                String placeType = firstNonBlank(
                        getText(columns, headerIndex, "place_type"),
                        getText(columns, headerIndex, "tag_place_type"),
                        getText(columns, headerIndex, "place_type_tag")
                );

                String lightingLevel = firstNonBlank(
                        getText(columns, headerIndex, "lighting_level"),
                        getText(columns, headerIndex, "tag_lighting_level"),
                        getText(columns, headerIndex, "lighting_tag")
                );

                Boolean quietSpaceAvailable = firstNonNull(
                        getBoolean(columns, headerIndex, "quiet_space_available"),
                        getBoolean(columns, headerIndex, "tag_quiet_space_available")
                );

                String overallSensoryScore = firstNonBlank(
                        getText(columns, headerIndex, "overall_sensory_score"),
                        getText(columns, headerIndex, "tag_overall_sensory_score")
                );

                String summary = firstNonBlank(
                        getText(columns, headerIndex, "summary"),
                        getText(columns, headerIndex, "tag_summary")
                );

                String goodToKnowTips = firstNonBlank(
                        getText(columns, headerIndex, "good_to_know_tips"),
                        getText(columns, headerIndex, "tag_good_to_know_tips"),
                        getText(columns, headerIndex, "good_to_know")
                );

                String escapeRouteNotes = firstNonBlank(
                        getText(columns, headerIndex, "escape_route_notes"),
                        getText(columns, headerIndex, "tag_escape_route_notes")
                );

                String bestTimesToVisit = firstNonBlank(
                        getText(columns, headerIndex, "best_times_to_visit"),
                        getText(columns, headerIndex, "tag_best_times_to_visit")
                );

                String lastReviewed = firstNonBlank(
                        getText(columns, headerIndex, "last_reviewed"),
                        getText(columns, headerIndex, "last_updated"),
                        getText(columns, headerIndex, "updated_at"),
                        getText(columns, headerIndex, "reviewed_at")
                );

                String source = firstNonBlank(
                        getText(columns, headerIndex, "source"),
                        "DS merged_locations_with_tags"
                );

                String confidence = firstNonBlank(
                        getText(columns, headerIndex, "confidence"),
                        getText(columns, headerIndex, "tag_confidence")
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
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
