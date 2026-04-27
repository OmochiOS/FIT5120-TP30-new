package com.fomopotato.amityspace.service;

import com.fomopotato.amityspace.data.record.LocationHistoricalCrowdPatternRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationHistoricalCrowdPatternDataService {

    private static final String CSV_PATH = "data/location_historical_crowd_pattern.csv";

    private Map<Long, List<LocationHistoricalCrowdPatternRecord>> cachedPatterns;

    /**
     * Returns all historical crowd pattern records grouped by locationId.
     */
    public Map<Long, List<LocationHistoricalCrowdPatternRecord>> getHistoricalPatternsByLocationId() {
        if (cachedPatterns == null) {
            cachedPatterns = loadHistoricalPatternsFromCsv();
        }
        return cachedPatterns;
    }

    /**
     * Returns historical crowd patterns for one locationId.
     */
    public List<LocationHistoricalCrowdPatternRecord> getHistoricalPatternsByLocationId(Long locationId) {
        if (locationId == null) {
            return new ArrayList<>();
        }
        return getHistoricalPatternsByLocationId().getOrDefault(locationId, new ArrayList<>());
    }

    /**
     * Returns true when at least one historical pattern exists for the location.
     */
    public boolean hasHistoricalPatternsForLocation(Long locationId) {
        return !getHistoricalPatternsByLocationId(locationId).isEmpty();
    }

    private Map<Long, List<LocationHistoricalCrowdPatternRecord>> loadHistoricalPatternsFromCsv() {
        Map<Long, List<LocationHistoricalCrowdPatternRecord>> patterns = new LinkedHashMap<>();

        try (InputStream inputStream = new ClassPathResource(CSV_PATH).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return patterns;
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
                String dayType = getText(columns, headerIndex, "day_type");
                String dayOfWeek = getText(columns, headerIndex, "day_of_week");
                String timeBlock = getText(columns, headerIndex, "time_block");

                Integer avgCrowdCount = getRoundedInteger(columns, headerIndex, "avg_crowd_count");
                Integer minCrowdCount = getInteger(columns, headerIndex, "min_crowd_count");
                Integer maxCrowdCount = getInteger(columns, headerIndex, "max_crowd_count");

                String crowdLevelLabel = getText(columns, headerIndex, "crowd_level_label");
                String predictabilityLevel = getText(columns, headerIndex, "predictability_level");
                Boolean recommendedVisitFlag = getBoolean(columns, headerIndex, "recommended_visit_flag");

                Integer sampleSize = getInteger(columns, headerIndex, "sample_size");
                String dateRangeStart = getText(columns, headerIndex, "date_range_start");
                String dateRangeEnd = getText(columns, headerIndex, "date_range_end");
                String updatedAt = getText(columns, headerIndex, "updated_at");
                String notes = getText(columns, headerIndex, "notes");

                LocationHistoricalCrowdPatternRecord record = new LocationHistoricalCrowdPatternRecord(
                        locationId,
                        locationName,
                        dayType,
                        dayOfWeek,
                        timeBlock,
                        avgCrowdCount,
                        minCrowdCount,
                        maxCrowdCount,
                        crowdLevelLabel,
                        predictabilityLevel,
                        recommendedVisitFlag,
                        sampleSize,
                        dateRangeStart,
                        dateRangeEnd,
                        updatedAt,
                        notes
                );

                patterns.computeIfAbsent(locationId, key -> new ArrayList<>()).add(record);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load historical crowd patterns from CSV: " + CSV_PATH, e);
        }

        return patterns;
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

    private Integer getInteger(String[] columns, Map<String, Integer> headerIndex, String columnName) {
        String value = getText(columns, headerIndex, columnName);
        if (isBlank(value)) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                return (int) Math.round(Double.parseDouble(value));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private Integer getRoundedInteger(String[] columns, Map<String, Integer> headerIndex, String columnName) {
        String value = getText(columns, headerIndex, columnName);
        if (isBlank(value)) {
            return null;
        }

        try {
            return (int) Math.round(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean getBoolean(String[] columns, Map<String, Integer> headerIndex, String columnName) {
        String value = getText(columns, headerIndex, columnName);
        if (isBlank(value)) {
            return null;
        }

        return Boolean.parseBoolean(value.trim());
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV parser that supports quoted values.
        // Sufficient for current project CSV files.
        StringBuilder current = new StringBuilder();
        List<String> values = new ArrayList<>();
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