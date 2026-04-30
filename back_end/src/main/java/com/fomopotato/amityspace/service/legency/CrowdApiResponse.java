package com.fomopotato.amityspace.service.legency;

public class CrowdApiResponse {
    private final Long locationId;
    private final int totalCount;

    public CrowdApiResponse(Long locationId, int totalCount) {
        this.locationId = locationId;
        this.totalCount = totalCount;
    }

    public Long getLocationId() {
        return locationId;
    }

    public int getTotalCount() {
        return totalCount;
    }
}