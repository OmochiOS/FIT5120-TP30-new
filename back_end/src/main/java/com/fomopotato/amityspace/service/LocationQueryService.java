package com.fomopotato.amityspace.service;

import com.fomopotato.amityspace.dto.LocationDetailDTO;
import com.fomopotato.amityspace.dto.LocationSummaryDTO;
import com.fomopotato.amityspace.dto.SensoryProfileDTO;
import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;
import com.fomopotato.amityspace.exception.LocationNotFoundException;
import com.fomopotato.amityspace.exception.SensoryProfileNotFoundException;
import com.fomopotato.amityspace.model.Location;
import com.fomopotato.amityspace.model.SensoryProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationQueryService {

    private final LocationDataService locationDataService;

    public LocationQueryService(LocationDataService locationDataService) {
        this.locationDataService = locationDataService;
    }

    public List<LocationSummaryDTO> getAllLocations() {
        List<LocationSummaryDTO> results = new ArrayList<>();

        for (Location location : getAllLocationModels()) {
            results.add(buildLocationSummary(location, false));
        }

        return results;
    }

    public List<Location> getAllLocationModels() {
        return locationDataService.getLocations();
    }

    public Location getLocationById(Long id) {
        for (Location location : getAllLocationModels()) {
            if (location.getId().equals(id)) {
                return location;
            }
        }
        throw new LocationNotFoundException(id);
    }

    public SensoryProfile getSensoryProfileForLocation(Location location) {
        SensoryProfile sensoryProfile = location.getSensoryProfile();

        if (sensoryProfile == null) {
            throw new SensoryProfileNotFoundException(location.getId());
        }

        return sensoryProfile;
    }

    public LocationDetailDTO getLocationDetailById(Long id) {
        Location location = getLocationById(id);
        SensoryProfile sensoryProfile = getSensoryProfileForLocation(location);

        return buildLocationDetail(location, sensoryProfile, false);
    }

    LocationSummaryDTO buildLocationSummary(Location location, boolean matchesPreferences) {
        return new LocationSummaryDTO(
                location.getId(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCrowdLevel(),
                location.getStatusLabel(),
                location.getPlaceType(),
                matchesPreferences
        );
    }

    LocationDetailDTO buildLocationDetail(Location location,
                                          SensoryProfile sensoryProfile,
                                          boolean matchesPreferences) {
        return new LocationDetailDTO(
                location.getId(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCrowdLevel(),
                location.getStatusLabel(),
                buildSensoryProfile(sensoryProfile),
                location.getPlaceType(),
                matchesPreferences
        );
    }

    SensoryProfileDTO buildSensoryProfile(SensoryProfile sensoryProfile) {
        return new SensoryProfileDTO(
                sensoryProfile.getNoiseDb(),
                getSensoryLabel(sensoryProfile.getNoiseLevel()),
                buildNoiseDescription(sensoryProfile),
                getSensoryLabel(sensoryProfile.getLightingLevel()),
                buildLightingDescription(sensoryProfile),
                getPredictabilityLabel(sensoryProfile.getPredictabilityLevel()),
                buildPredictabilityDescription(sensoryProfile),
                sensoryProfile.isQuietSpaceAvailable(),
                buildQuietSpaceDescription(sensoryProfile),
                sensoryProfile.getOverallSensoryScore(),
                sensoryProfile.getBestTimesToVisit(),
                sensoryProfile.getGoodToKnowTips(),
                sensoryProfile.getLastUpdated()
        );
    }

    private String getSensoryLabel(SensoryLevel level) {
        if (level == null) {
            return "Moderate";
        }

        switch (level) {
            case LOW:
                return "Calm";
            case MODERATE:
                return "Moderate";
            case HIGH:
                return "Busy";
            case OVERSTIMULATING:
                return "Overstimulating";
            default:
                return "Moderate";
        }
    }

    private String getPredictabilityLabel(PredictabilityLevel level) {
        if (level == null) {
            return "Moderate";
        }

        switch (level) {
            case HIGH:
                return "High";
            case MODERATE:
                return "Moderate";
            case LOW:
                return "Low";
            default:
                return "Moderate";
        }
    }

    private String buildNoiseDescription(SensoryProfile sensoryProfile) {
        if (sensoryProfile == null || sensoryProfile.getNoiseLevel() == null) {
            return "Noise is estimated from the latest available context.";
        }

        switch (sensoryProfile.getNoiseLevel()) {
            case LOW:
                return "Quiet environment with minimal background noise.";
            case MODERATE:
                return "Some noticeable sound is present, but it is usually manageable.";
            case HIGH:
                return "Noise levels are high and may feel distracting or tiring.";
            case OVERSTIMULATING:
                return "Very loud environment that may be overwhelming for sensory-sensitive users.";
            default:
                return "Noise is estimated from the latest available context.";
        }
    }

    private String buildLightingDescription(SensoryProfile sensoryProfile) {
        if (sensoryProfile == null || sensoryProfile.getLightingLevel() == null) {
            return "Lighting may vary; use local conditions as the guide.";
        }

        switch (sensoryProfile.getLightingLevel()) {
            case LOW:
                return "Soft or dim lighting that may feel calmer.";
            case MODERATE:
                return "Moderate lighting that is generally comfortable for most users.";
            case HIGH:
                return "Bright lighting that may feel intense for light-sensitive users.";
            case OVERSTIMULATING:
                return "Very bright or harsh lighting that may feel overwhelming.";
            default:
                return "Lighting may vary; use local conditions as the guide.";
        }
    }

    private String buildPredictabilityDescription(SensoryProfile sensoryProfile) {
        if (sensoryProfile == null || sensoryProfile.getPredictabilityLevel() == null) {
            return "Visit patterns may vary; use the current crowd score as the main guide.";
        }

        switch (sensoryProfile.getPredictabilityLevel()) {
            case HIGH:
                return "The environment is structured and changes are usually predictable.";
            case MODERATE:
                return "The environment is somewhat stable, though occasional changes may occur.";
            case LOW:
                return "The environment may change frequently and feel less predictable.";
            default:
                return "Visit patterns may vary; use the current crowd score as the main guide.";
        }
    }

    private String buildQuietSpaceDescription(SensoryProfile sensoryProfile) {
        if (sensoryProfile == null) {
            return "No dedicated quiet space is listed; plan a nearby reset point.";
        }

        if (sensoryProfile.isQuietSpaceAvailable()) {
            return "A quieter area is available nearby if needed.";
        }
        return "No dedicated quiet space is listed; plan a nearby reset point.";
    }
}
