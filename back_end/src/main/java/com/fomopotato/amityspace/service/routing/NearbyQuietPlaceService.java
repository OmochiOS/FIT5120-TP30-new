package com.fomopotato.amityspace.service.routing;

import com.fomopotato.amityspace.dto.NearbyQuietPlaceDTO;
import com.fomopotato.amityspace.entity.LocationEntity;
import com.fomopotato.amityspace.entity.LocationNearbyPointEntity;
import com.fomopotato.amityspace.entity.SensoryProfileEntity;
import com.fomopotato.amityspace.exception.LocationNotFoundException;
import com.fomopotato.amityspace.repository.LocationNearbyPointRepository;
import com.fomopotato.amityspace.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NearbyQuietPlaceService {

    private static final double DEFAULT_RADIUS_METERS = 200.0;
    private static final double MAX_RADIUS_METERS = 1000.0;

    private final LocationRepository locationRepository;
    private final LocationNearbyPointRepository locationNearbyPointRepository;

    public NearbyQuietPlaceService(LocationRepository locationRepository,
                                   LocationNearbyPointRepository locationNearbyPointRepository) {
        this.locationRepository = locationRepository;
        this.locationNearbyPointRepository = locationNearbyPointRepository;
    }

    @Transactional(readOnly = true)
    public List<NearbyQuietPlaceDTO> findNearbyQuietPlaces(Long destinationId, Double radiusMeters) {
        LocationEntity destination = locationRepository.findById(destinationId)
                .orElseThrow(() -> new LocationNotFoundException(destinationId));

        double searchRadius = resolveRadiusMeters(radiusMeters);

        return locationNearbyPointRepository
                .findBySourceLocationIdOrderByDistanceMetersAsc(destination.getId())
                .stream()
                .filter(nearbyPoint -> nearbyPoint.getDistanceMeters() != null)
                .filter(nearbyPoint -> nearbyPoint.getDistanceMeters() <= searchRadius)
                .map(this::buildNearbyQuietPlaceDTO)
                .filter(dto -> isCalmerSensoryScore(dto.getSensoryScore()))
                .toList();
    }

    private double resolveRadiusMeters(Double radiusMeters) {
        if (radiusMeters == null) {
            return DEFAULT_RADIUS_METERS;
        }

        if (radiusMeters <= 0 || radiusMeters > MAX_RADIUS_METERS) {
            throw new IllegalArgumentException("Radius must be greater than 0 and no more than 1000 metres.");
        }

        return radiusMeters;
    }

    private NearbyQuietPlaceDTO buildNearbyQuietPlaceDTO(LocationNearbyPointEntity nearbyPoint) {
        LocationEntity nearbyLocation = locationRepository.findById(nearbyPoint.getNearbyLocationId())
                .orElseThrow(() -> new LocationNotFoundException(nearbyPoint.getNearbyLocationId()));

        SensoryProfileEntity sensoryProfile = nearbyLocation.getSensoryProfile();

        String sensoryScore = sensoryProfile == null ? null : sensoryProfile.getOverallSensoryScore();
        String sensoryLabel = buildSensoryLabel(sensoryScore);

        return new NearbyQuietPlaceDTO(
                nearbyLocation.getId(),
                nearbyLocation.getName(),
                nearbyLocation.getLatitude(),
                nearbyLocation.getLongitude(),
                sensoryScore,
                sensoryLabel,
                roundDistance(nearbyPoint.getDistanceMeters()),
                "Calmer nearby option"
        );
    }

    private boolean isCalmerSensoryScore(String sensoryScore) {
        if (sensoryScore == null || sensoryScore.isBlank()) {
            return false;
        }

        String normalized = sensoryScore.trim().toLowerCase();

        return normalized.startsWith("1")
                || normalized.startsWith("2")
                || normalized.contains("calm")
                || normalized.contains("moderate")
                || normalized.contains("manageable");
    }

    private String buildSensoryLabel(String sensoryScore) {
        if (sensoryScore == null || sensoryScore.isBlank()) {
            return "Unknown";
        }

        String normalized = sensoryScore.trim().toLowerCase();

        if (normalized.startsWith("1") || normalized.startsWith("2") || normalized.contains("calm")) {
            return "Calm";
        }

        if (normalized.startsWith("3")
                || normalized.contains("moderate")
                || normalized.contains("manageable")) {
            return "Manageable";
        }

        if (normalized.startsWith("4")
                || normalized.contains("busy")
                || normalized.contains("overstimulating")) {
            return "Busy";
        }

        return "Unknown";
    }

    private double roundDistance(double distanceMeters) {
        return Math.round(distanceMeters * 10.0) / 10.0;
    }
}