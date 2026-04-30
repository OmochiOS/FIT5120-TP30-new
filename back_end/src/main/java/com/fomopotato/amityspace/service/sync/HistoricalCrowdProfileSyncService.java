package com.fomopotato.amityspace.service.sync;

import com.fomopotato.amityspace.entity.HistoricalCrowdPatternEntity;
import com.fomopotato.amityspace.entity.SensoryProfileEntity;
import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.repository.HistoricalCrowdPatternRepository;
import com.fomopotato.amityspace.repository.SensoryProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HistoricalCrowdProfileSyncService {

    private final HistoricalCrowdPatternRepository historicalCrowdPatternRepository;
    private final SensoryProfileRepository sensoryProfileRepository;

    public HistoricalCrowdProfileSyncService(
            HistoricalCrowdPatternRepository historicalCrowdPatternRepository,
            SensoryProfileRepository sensoryProfileRepository) {
        this.historicalCrowdPatternRepository = historicalCrowdPatternRepository;
        this.sensoryProfileRepository = sensoryProfileRepository;
    }

    public int syncHistoricalCrowdProfiles() {
        List<SensoryProfileEntity> profiles = sensoryProfileRepository.findAll();

        int updatedCount = 0;

        for (SensoryProfileEntity profile : profiles) {
            Long locationId = profile.getLocationId();

            if (locationId == null) {
                continue;
            }

            List<HistoricalCrowdPatternEntity> patterns =
                    historicalCrowdPatternRepository.findByLocationId(locationId);

            if (patterns == null || patterns.isEmpty()) {
                continue;
            }

            Optional<HistoricalCrowdPatternEntity> quietestPattern = patterns.stream()
                    .filter(pattern -> pattern.getAvgCount() != null)
                    .min(Comparator.comparing(HistoricalCrowdPatternEntity::getAvgCount));

            if (quietestPattern.isEmpty()) {
                continue;
            }

            HistoricalCrowdPatternEntity bestPattern = quietestPattern.get();

            profile.setBestTimesToVisit(buildBestTimeLabel(bestPattern));
            profile.setPredictabilityLevel(resolvePredictabilityLevel(patterns));

            sensoryProfileRepository.save(profile);
            updatedCount++;
        }

        return updatedCount;
    }

    private String buildBestTimeLabel(HistoricalCrowdPatternEntity pattern) {
        String dayLabel = pattern.getDayType();

        if (dayLabel == null || dayLabel.isBlank()) {
            dayLabel = pattern.getDayOfWeek();
        }

        if (dayLabel == null || dayLabel.isBlank()) {
            dayLabel = "Typical";
        }

        Integer hour = pattern.getHour();

        if (hour == null) {
            return dayLabel + " quieter periods";
        }

        return dayLabel + " around " + formatHour(hour);
    }

    private String formatHour(Integer hour) {
        if (hour == null) {
            return "unknown time";
        }

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

    private PredictabilityLevel resolvePredictabilityLevel(List<HistoricalCrowdPatternEntity> patterns) {
        double totalCv = 0.0;
        int validCount = 0;

        for (HistoricalCrowdPatternEntity pattern : patterns) {
            Double avg = pattern.getAvgCount();
            Double std = pattern.getStdCount();

            if (avg == null || std == null || avg <= 0) {
                continue;
            }

            totalCv += std / avg;
            validCount++;
        }

        if (validCount == 0) {
            return PredictabilityLevel.MODERATE;
        }

        double averageCv = totalCv / validCount;

        if (averageCv < 0.3) {
            return PredictabilityLevel.HIGH;
        }

        if (averageCv <= 0.5) {
            return PredictabilityLevel.MODERATE;
        }

        return PredictabilityLevel.LOW;
    }
}