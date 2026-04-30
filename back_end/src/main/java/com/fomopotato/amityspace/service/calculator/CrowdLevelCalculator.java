package com.fomopotato.amityspace.service.calculator;

import com.fomopotato.amityspace.enums.CrowdLevel;
import org.springframework.stereotype.Service;

@Service
public class CrowdLevelCalculator {

    public CrowdLevel resolveCrowdLevel(Integer count) {
        if (count == null) {
            return CrowdLevel.MODERATE;
        }

        if (count <= 68) {
            return CrowdLevel.LOW;
        }

        if (count <= 316) {
            return CrowdLevel.MODERATE;
        }

        return CrowdLevel.HIGH;
    }

    public String buildStatusLabel(CrowdLevel crowdLevel) {
        if (crowdLevel == null) {
            return "Status unavailable";
        }

        switch (crowdLevel) {
            case LOW:
                return "Calm now";
            case MODERATE:
                return "Moderate now";
            case HIGH:
                return "Busy now";
            default:
                return "Status unavailable";
        }
    }
}