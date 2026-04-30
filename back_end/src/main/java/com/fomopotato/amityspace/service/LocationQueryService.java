package com.fomopotato.amityspace.service;

import com.fomopotato.amityspace.dto.LocationDetailDTO;
import com.fomopotato.amityspace.dto.LocationSummaryDTO;
import com.fomopotato.amityspace.dto.SensoryProfileDTO;
import com.fomopotato.amityspace.entity.LocationEntity;
import com.fomopotato.amityspace.entity.SensoryProfileEntity;
import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;
import com.fomopotato.amityspace.exception.LocationNotFoundException;
import com.fomopotato.amityspace.exception.SensoryProfileNotFoundException;
import com.fomopotato.amityspace.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationQueryService {

    private final LocationRepository locationRepository;

    public LocationQueryService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationSummaryDTO> getAllLocations() {
        List<LocationSummaryDTO> results = new ArrayList<>();

        for (LocationEntity location : getAllLocationEntities()) {
            results.add(buildLocationSummary(location, false));
        }

        return results;
    }

    public List<LocationEntity> getAllLocationEntities() {
        return locationRepository.findAll();
    }

    public LocationEntity getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    public SensoryProfileEntity getSensoryProfileForLocation(LocationEntity location) {
        SensoryProfileEntity sensoryProfile = location.getSensoryProfile();

        if (sensoryProfile == null) {
            throw new SensoryProfileNotFoundException(location.getId());
        }

        return sensoryProfile;
    }

    public LocationDetailDTO getLocationDetailById(Long id) {
        LocationEntity location = getLocationById(id);
        SensoryProfileEntity sensoryProfile = getSensoryProfileForLocation(location);

        return buildLocationDetail(location, sensoryProfile, false);
    }

    LocationSummaryDTO buildLocationSummary(LocationEntity location, boolean matchesPreferences) {
        return new LocationSummaryDTO(
                location.getId(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCrowdLevel(),
                location.getStatusLabel(),
                location.getPlaceType(),
                location.getDescription(),
                matchesPreferences
        );
    }

    LocationDetailDTO buildLocationDetail(LocationEntity location,
                                          SensoryProfileEntity sensoryProfile,
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

    SensoryProfileDTO buildSensoryProfile(SensoryProfileEntity sensoryProfile) {
        return new SensoryProfileDTO(
                sensoryProfile.getNoiseDb(),
                getSensoryLabel(sensoryProfile.getNoiseLevel()),
                buildNoiseDescription(sensoryProfile),
                getSensoryLabel(sensoryProfile.getLightingLevel()),
                buildLightingDescription(sensoryProfile),
                getPredictabilityLabel(sensoryProfile.getPredictabilityLevel()),
                buildPredictabilityDescription(sensoryProfile),
                sensoryProfile.getQuietSpaceAvailable(),
                buildQuietSpaceDescription(sensoryProfile),
                sensoryProfile.getOverallSensoryScore(),
                sensoryProfile.getBestTimesToVisit(),
                sensoryProfile.getGoodToKnowTips(),
                sensoryProfile.getLastUpdated()
        );
    }

    private String getSensoryLabel(SensoryLevel level) {
        if (level == null) {
            return null;
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
                return null;
        }
    }

    private String getPredictabilityLabel(PredictabilityLevel level) {
        if (level == null) {
            return null;
        }

        switch (level) {
            case HIGH:
                return "High";
            case MODERATE:
                return "Moderate";
            case LOW:
                return "Low";
            default:
                return null;
        }
    }

    private String buildNoiseDescription(SensoryProfileEntity sensoryProfile) {
        if (sensoryProfile == null || sensoryProfile.getNoiseLevel() == null) {
            return "Noise conditions are not available.";
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
                return "Noise conditions are not available.";
        }
    }

    private String buildLightingDescription(SensoryProfileEntity sensoryProfile) {
        if (sensoryProfile == null || sensoryProfile.getLightingLevel() == null) {
            return "Lighting conditions are not available.";
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
                return "Lighting conditions are not available.";
        }
    }

    private String buildPredictabilityDescription(SensoryProfileEntity sensoryProfile) {
        if (sensoryProfile == null || sensoryProfile.getPredictabilityLevel() == null) {
            return "Predictability information is not available.";
        }

        switch (sensoryProfile.getPredictabilityLevel()) {
            case HIGH:
                return "The environment is structured and changes are usually predictable.";
            case MODERATE:
                return "The environment is somewhat stable, though occasional changes may occur.";
            case LOW:
                return "The environment may change frequently and feel less predictable.";
            default:
                return "Predictability information is not available.";
        }
    }

    private String buildQuietSpaceDescription(SensoryProfileEntity sensoryProfile) {
        if (sensoryProfile == null || sensoryProfile.getQuietSpaceAvailable() == null) {
            return "Quiet space information is not available.";
        }

        if (Boolean.TRUE.equals(sensoryProfile.getQuietSpaceAvailable())) {
            return "A quieter area is available nearby if needed.";
        }

        return "No dedicated quiet space is currently available.";
    }
}