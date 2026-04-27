package com.fomopotato.amityspace.service;

import com.fomopotato.amityspace.dto.LocationDetailDTO;
import com.fomopotato.amityspace.dto.LocationSummaryDTO;
import com.fomopotato.amityspace.dto.PreferenceRequestDTO;
import com.fomopotato.amityspace.model.Location;
import com.fomopotato.amityspace.model.SensoryProfile;
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
        return processLocations(request, false);
    }

    public List<LocationSummaryDTO> filterLocations(PreferenceRequestDTO request) {
        return processLocations(request, true);
    }

    public LocationDetailDTO getLocationDetailById(Long id, PreferenceRequestDTO request) {
        Location location = locationQueryService.getLocationById(id);
        SensoryProfile sensoryProfile = locationQueryService.getSensoryProfileForLocation(location);
        boolean matchesPreferences = locationFilterService.matchesPreferences(location, sensoryProfile, request);

        return locationQueryService.buildLocationDetail(location, sensoryProfile, matchesPreferences);
    }

    private List<LocationSummaryDTO> processLocations(PreferenceRequestDTO request, boolean filterOnly) {
        List<LocationSummaryDTO> results = new ArrayList<>();

        for (Location location : locationQueryService.getAllLocationModels()) {
            SensoryProfile sensoryProfile = locationQueryService.getSensoryProfileForLocation(location);
            boolean matchesPreferences = locationFilterService.matchesPreferences(location, sensoryProfile, request);

            if (filterOnly && !matchesPreferences) {
                continue;
            }

            results.add(locationQueryService.buildLocationSummary(location, matchesPreferences));
        }

        return results;
    }
}
