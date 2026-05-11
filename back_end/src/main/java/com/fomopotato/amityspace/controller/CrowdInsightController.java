package com.fomopotato.amityspace.controller;

import com.fomopotato.amityspace.dto.CrowdInsightDTO;
import com.fomopotato.amityspace.service.crowd.CrowdInsightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class CrowdInsightController {

    private final CrowdInsightService crowdInsightService;

    public CrowdInsightController(CrowdInsightService crowdInsightService) {
        this.crowdInsightService = crowdInsightService;
    }

    @GetMapping("/{locationId}/crowd-insight")
    public CrowdInsightDTO getCrowdInsight(@PathVariable Long locationId,
                                           @RequestParam Integer visitHour) {
        return crowdInsightService.getCrowdInsight(locationId, visitHour);
    }
}