package com.fomopotato.amityspace.dto;

public class NearbyQuietPlaceDTO {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String sensoryScore;
    private String sensoryLabel;
    private Double distanceMeters;
    private String label;

    public NearbyQuietPlaceDTO() {
    }

    public NearbyQuietPlaceDTO(Long id,
                               String name,
                               Double latitude,
                               Double longitude,
                               String sensoryScore,
                               String sensoryLabel,
                               Double distanceMeters,
                               String label) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sensoryScore = sensoryScore;
        this.sensoryLabel = sensoryLabel;
        this.distanceMeters = distanceMeters;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getSensoryScore() {
        return sensoryScore;
    }

    public String getSensoryLabel() {
        return sensoryLabel;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public String getLabel() {
        return label;
    }
}