package com.fomopotato.amityspace.service.legency;

public class NoiseApiResponse {
    private final double latitude;
    private final double longitude;
    private final double noiseDb;

    public NoiseApiResponse(double latitude, double longitude, double noiseDb) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.noiseDb = noiseDb;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getNoiseDb() {
        return noiseDb;
    }
}