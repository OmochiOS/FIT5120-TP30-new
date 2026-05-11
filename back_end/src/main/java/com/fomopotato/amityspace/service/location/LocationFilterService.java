package com.fomopotato.amityspace.service.location;

import com.fomopotato.amityspace.dto.PreferenceRequestDTO;
import com.fomopotato.amityspace.entity.LocationEntity;
import com.fomopotato.amityspace.entity.SensoryProfileEntity;
import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;
import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;
import org.springframework.stereotype.Service;

@Service
public class LocationFilterService {

    public boolean matchesPreferences(LocationEntity location,
                                      SensoryProfileEntity sensoryProfile,
                                      PreferenceRequestDTO request) {
        if (request == null) {
            return true;
        }

        if (location == null || sensoryProfile == null) {
            return false;
        }

        if (Boolean.TRUE.equals(request.getLowCrowd()) && location.getCrowdLevel() != CrowdLevel.LOW) {
            return false;
        }

        if (Boolean.TRUE.equals(request.getLowNoise())) {
            SensoryLevel noiseLevel = sensoryProfile.getNoiseLevel();

            if (noiseLevel != SensoryLevel.LOW && noiseLevel != SensoryLevel.MODERATE) {
                return false;
            }
        }

        if (Boolean.TRUE.equals(request.getLowLighting())) {
            SensoryLevel lightingLevel = sensoryProfile.getLightingLevel();

            if (lightingLevel != SensoryLevel.LOW && lightingLevel != SensoryLevel.MODERATE) {
                return false;
            }
        }

        if (Boolean.TRUE.equals(request.getQuietSpaceRequired()) && !Boolean.TRUE.equals(sensoryProfile.getQuietSpaceAvailable())) {
            return false;
        }

        if (Boolean.TRUE.equals(request.getPredictableEnvironment())) {
            PredictabilityLevel level = sensoryProfile.getPredictabilityLevel();

            if (level != PredictabilityLevel.HIGH && level != PredictabilityLevel.MODERATE) {
                return false;
            }
        }

        if (request.getPlaceType() != null && request.getPlaceType() != PlaceType.ANY && location.getPlaceType() != request.getPlaceType()) {
            return false;
        }

        return true;
    }
}