package com.fomopotato.amityspace.service.calculator;

import com.fomopotato.amityspace.enums.PredictabilityLevel;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PredictabilityCalculator {

    public PredictabilityLevel resolvePredictability(List<Integer> series) {
        if (series == null || series.size() < 3) {
            return PredictabilityLevel.MODERATE;
        }

        int min = Collections.min(series);
        int max = Collections.max(series);
        int range = max - min;

        if (range < 20) {
            return PredictabilityLevel.HIGH;
        }

        if (range < 60) {
            return PredictabilityLevel.MODERATE;
        }

        return PredictabilityLevel.LOW;
    }
}