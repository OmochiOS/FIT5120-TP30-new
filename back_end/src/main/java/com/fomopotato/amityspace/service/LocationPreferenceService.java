package com.fomopotato.amityspace.service;

import com.fomopotato.amityspace.dto.LocationSummaryDTO;
import com.fomopotato.amityspace.dto.PreferenceRequestDTO;
import com.fomopotato.amityspace.entity.LocationEntity;
import com.fomopotato.amityspace.entity.SensoryProfileEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationPreferenceService {

    private final LocationQueryService locationQueryService;
    private final LocationFilterService locationFilterService;

    public LocationPreferenceService(LocationQueryService locationQueryService,
                                     LocationFilterService locationFilterService) {
        this.locationQueryService = locationQueryService;
        this.locationFilterService = locationFilterService;
    }

    public List<LocationSummaryDTO> matchLocations(PreferenceRequestDTO request) {
        List<LocationSummaryDTO> results = new ArrayList<>();

        for (LocationEntity location : locationQueryService.getAllLocationEntities()) {
            SensoryProfileEntity sensoryProfile = locationQueryService.getSensoryProfileForLocation(location);

            boolean matchesPreferences = locationFilterService.matchesPreferences(
                    location,
                    sensoryProfile,
                    request
            );

            results.add(locationQueryService.buildLocationSummary(location, matchesPreferences));
        }

        return results;
    }

    public List<LocationSummaryDTO> filterLocations(PreferenceRequestDTO request) {
        List<LocationSummaryDTO> results = new ArrayList<>();

        for (LocationEntity location : locationQueryService.getAllLocationEntities()) {
            SensoryProfileEntity sensoryProfile = locationQueryService.getSensoryProfileForLocation(location);

            boolean matchesPreferences = locationFilterService.matchesPreferences(
                    location,
                    sensoryProfile,
                    request
            );

            if (matchesPreferences) {
                results.add(locationQueryService.buildLocationSummary(location, true));
            }
        }

        return results;
    }
}