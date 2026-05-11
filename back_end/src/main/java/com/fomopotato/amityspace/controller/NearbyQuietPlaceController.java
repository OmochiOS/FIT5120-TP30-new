package com.fomopotato.amityspace.controller;

import com.fomopotato.amityspace.dto.NearbyQuietPlaceDTO;
import com.fomopotato.amityspace.service.routing.NearbyQuietPlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class NearbyQuietPlaceController {

    private final NearbyQuietPlaceService nearbyQuietPlaceService;

    public NearbyQuietPlaceController(NearbyQuietPlaceService nearbyQuietPlaceService) {
        this.nearbyQuietPlaceService = nearbyQuietPlaceService;
    }

    @GetMapping("/{destinationId}/nearby-quiet-places")
    public List<NearbyQuietPlaceDTO> getNearbyQuietPlaces(
            @PathVariable Long destinationId,
            @RequestParam(required = false) Double radiusMeters) {
        return nearbyQuietPlaceService.findNearbyQuietPlaces(destinationId, radiusMeters);
    }
}