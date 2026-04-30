package com.fomopotato.amityspace.integration.client;

import com.fomopotato.amityspace.integration.dto.CrowdCountApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MelbourneCrowdApiClient {

    private static final String CROWD_API_URL =
            "https://data.melbourne.vic.gov.au/api/explore/v2.1/catalog/datasets/pedestrian-counting-system-past-hour-counts-per-minute/records?limit=100";

    private final RestTemplate restTemplate;

    public MelbourneCrowdApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CrowdCountApiResponse fetchRecentCrowdCounts() {
        return restTemplate.getForObject(CROWD_API_URL, CrowdCountApiResponse.class);
    }
}