package com.fomopotato.amityspace.service.crowd;

import com.fomopotato.amityspace.dto.CrowdInsightDTO;
import com.fomopotato.amityspace.entity.HistoricalCrowdPatternEntity;
import com.fomopotato.amityspace.exception.LocationNotFoundException;
import com.fomopotato.amityspace.repository.HistoricalCrowdPatternRepository;
import com.fomopotato.amityspace.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CrowdInsightService {

    private static final int MIN_VISIT_HOUR = 7;
    private static final int MAX_VISIT_HOUR = 21;

    private static final double LOW_CROWD_MAX = 68;
    private static final double MODERATE_CROWD_MAX = 316;

    private final HistoricalCrowdPatternRepository historicalCrowdPatternRepository;
    private final LocationRepository locationRepository;

    public CrowdInsightService(HistoricalCrowdPatternRepository historicalCrowdPatternRepository,
                               LocationRepository locationRepository) {
        this.historicalCrowdPatternRepository = historicalCrowdPatternRepository;
        this.locationRepository = locationRepository;
    }

    public CrowdInsightDTO getCrowdInsight(Long locationId, Integer visitHour) {
        validateLocation(locationId);
        validateVisitHour(visitHour);

        List<HistoricalCrowdPatternEntity> patterns =
                historicalCrowdPatternRepository.findByLocationIdAndHour(locationId, visitHour);

        if (patterns == null || patterns.isEmpty()) {
            return buildUnavailableResponse(locationId, visitHour);
        }

        HistoricalCrowdPatternEntity selectedPattern = chooseMostReliablePattern(patterns);

        String crowdLevel = resolveCrowdLevel(selectedPattern);
        String suitabilityLabel = buildSuitabilityLabel(crowdLevel);
        String alternativeSuggestion = buildAlternativeSuggestion(
                locationId,
                visitHour,
                selectedPattern,
                crowdLevel
        );

        return new CrowdInsightDTO(
                locationId,
                visitHour,
                formatHour(visitHour),
                crowdLevel,
                suitabilityLabel,
                alternativeSuggestion,
                true,
                "Crowd insight is estimated from available historical pedestrian data."
        );
    }

    private void validateLocation(Long locationId) {
        if (locationId == null || !locationRepository.existsById(locationId)) {
            throw new LocationNotFoundException(locationId);
        }
    }

    private void validateVisitHour(Integer visitHour) {
        if (visitHour == null || visitHour < MIN_VISIT_HOUR || visitHour > MAX_VISIT_HOUR) {
            throw new IllegalArgumentException("Visit hour must be between 7 and 21.");
        }
    }

    private HistoricalCrowdPatternEntity chooseMostReliablePattern(List<HistoricalCrowdPatternEntity> patterns) {
        return patterns.stream()
                .max(Comparator.comparing(pattern -> pattern.getSampleSize() == null ? 0 : pattern.getSampleSize()))
                .orElse(patterns.get(0));
    }

    private String resolveCrowdLevel(HistoricalCrowdPatternEntity pattern) {
        String label = pattern.getCrowdLevelLabel();

        if (label != null && !label.isBlank()) {
            return mapCrowdLevelLabel(label);
        }

        Double avgCount = pattern.getAvgCount();

        if (avgCount == null) {
            return "UNKNOWN";
        }

        if (avgCount <= LOW_CROWD_MAX) {
            return "LOW";
        }

        if (avgCount <= MODERATE_CROWD_MAX) {
            return "MODERATE";
        }

        return "HIGH";
    }

    private String mapCrowdLevelLabel(String label) {
        String normalized = label.trim().toLowerCase();

        if ("low".equals(normalized)) {
            return "LOW";
        }

        if ("medium".equals(normalized) || "moderate".equals(normalized)) {
            return "MODERATE";
        }

        if ("high".equals(normalized)) {
            return "HIGH";
        }

        return "UNKNOWN";
    }

    private String buildSuitabilityLabel(String crowdLevel) {
        if ("LOW".equals(crowdLevel)) {
            return "Likely calmer";
        }

        if ("MODERATE".equals(crowdLevel)) {
            return "Manageable";
        }

        if ("HIGH".equals(crowdLevel)) {
            return "May feel busy";
        }

        return "Crowd level unavailable";
    }

    private String buildAlternativeSuggestion(Long locationId,
                                              Integer visitHour,
                                              HistoricalCrowdPatternEntity selectedPattern,
                                              String crowdLevel) {
        if (!"HIGH".equals(crowdLevel)) {
            return null;
        }

        Double selectedAvgCount = selectedPattern.getAvgCount();

        if (selectedAvgCount == null) {
            return "No clearly calmer time is available from current data.";
        }

        List<HistoricalCrowdPatternEntity> allPatterns =
                historicalCrowdPatternRepository.findByLocationId(locationId);
        Optional<HistoricalCrowdPatternEntity> calmerPattern = allPatterns.stream()
            .filter(pattern -> pattern.getHour() != null)
            .filter(pattern -> pattern.getAvgCount() != null)
            .filter(pattern -> !pattern.getHour().equals(visitHour))
            .filter(pattern -> pattern.getAvgCount() < selectedAvgCount)
            .min(Comparator.comparing(pattern -> Math.abs(pattern.getHour() - visitHour)));

        if (calmerPattern.isEmpty()) {
            return "No clearly calmer time is available from current data.";
        }

        Integer suggestedHour = calmerPattern.get().getHour();

        if (suggestedHour < visitHour) {
            return "Earlier around " + formatHour(suggestedHour) + " may be calmer.";
        }

        return "Later around " + formatHour(suggestedHour) + " may be calmer.";
    }

    private CrowdInsightDTO buildUnavailableResponse(Long locationId, Integer visitHour) {
        return new CrowdInsightDTO(
                locationId,
                visitHour,
                formatHour(visitHour),
                "UNKNOWN",
                "Crowd level unavailable",
                null,
                false,
                "Crowd insight is unavailable for this time. Results are based on available AmitySpace data only."
        );
    }

    private String formatHour(Integer hour) {
        int safeHour = Math.floorMod(hour, 24);

        if (safeHour == 0) {
            return "12 AM";
        }

        if (safeHour < 12) {
            return safeHour + " AM";
        }

        if (safeHour == 12) {
            return "12 PM";
        }

        return (safeHour - 12) + " PM";
    }
}