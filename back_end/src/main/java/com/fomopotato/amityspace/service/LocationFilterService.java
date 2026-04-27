package com.fomopotato.amityspace.service;

import com.fomopotato.amityspace.dto.PreferenceRequestDTO;
import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;
import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;
import com.fomopotato.amityspace.model.Location;
import com.fomopotato.amityspace.model.SensoryProfile;
import org.springframework.stereotype.Service;

@Service
public class LocationFilterService {

    public boolean matchesPreferences(Location location, SensoryProfile sensoryProfile, PreferenceRequestDTO request) {
        if (request == null) {
            return true;
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
        
        if (Boolean.TRUE.equals(request.getQuietSpaceRequired()) && !sensoryProfile.isQuietSpaceAvailable()) {
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