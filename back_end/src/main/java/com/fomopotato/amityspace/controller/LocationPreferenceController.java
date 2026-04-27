package com.fomopotato.amityspace.controller;

import com.fomopotato.amityspace.dto.LocationSummaryDTO;
import com.fomopotato.amityspace.dto.PreferenceRequestDTO;
import com.fomopotato.amityspace.service.LocationPreferenceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationPreferenceController {

    private final LocationPreferenceService locationPreferenceService;

    public LocationPreferenceController(LocationPreferenceService locationPreferenceService) {
        this.locationPreferenceService = locationPreferenceService;
    }

    @PostMapping("/match")
    public List<LocationSummaryDTO> matchLocations(@RequestBody PreferenceRequestDTO request) {
        return locationPreferenceService.matchLocations(request);
    }

    @PostMapping("/filter")
    public List<LocationSummaryDTO> filterLocations(@RequestBody PreferenceRequestDTO request) {
        return locationPreferenceService.filterLocations(request);
    }
}
