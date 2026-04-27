package com.fomopotato.amityspace.controller;

import com.fomopotato.amityspace.dto.LocationDetailDTO;
import com.fomopotato.amityspace.dto.LocationSummaryDTO;
import com.fomopotato.amityspace.service.LocationQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationQueryService locationQueryService;

    public LocationController(LocationQueryService locationQueryService) {
        this.locationQueryService = locationQueryService;
    }

    @GetMapping
    public List<LocationSummaryDTO> getAllLocations() {
        return locationQueryService.getAllLocations();
    }

    @GetMapping("/{id}")
    public LocationDetailDTO getLocationDetailById(@PathVariable Long id) {
        return locationQueryService.getLocationDetailById(id);
    }
}
