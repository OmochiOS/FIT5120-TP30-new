package com.fomopotato.amityspace.service.legency;

public class LocationApiResponse {
    private final Long locationId;
    private final String name;
    private final double latitude;
    private final double longitude;

    public LocationApiResponse(Long locationId, String name, double latitude, double longitude) {
        this.locationId = locationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}