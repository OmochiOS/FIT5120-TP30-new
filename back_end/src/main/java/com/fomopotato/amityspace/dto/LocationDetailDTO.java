package com.fomopotato.amityspace.dto;

import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;

public class LocationDetailDTO {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private CrowdLevel crowdLevel;
    private String statusLabel;
    private SensoryProfileDTO sensoryProfile;
    private boolean matchesPreferences;
    private PlaceType placeType;

    public LocationDetailDTO(Long id, String name, double latitude, double longitude, CrowdLevel crowdLevel, String statusLabel, SensoryProfileDTO sensoryProfile, PlaceType placeType, boolean matchesPreferences) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.crowdLevel = crowdLevel;
        this.statusLabel = statusLabel;
        this.sensoryProfile = sensoryProfile;
        this.matchesPreferences = matchesPreferences;
        this.placeType = placeType;
    }

    public LocationDetailDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public CrowdLevel getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(CrowdLevel crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public SensoryProfileDTO getSensoryProfile() {
        return sensoryProfile;
    }

    public void setSensoryProfile(SensoryProfileDTO sensoryProfile) {
        this.sensoryProfile = sensoryProfile;
    }

    public boolean getMatchesPreferences(){
        return matchesPreferences;
    }

    public void setMatchesPreerences(boolean matchesPreferences){
        this.matchesPreferences = matchesPreferences;
    }

    public PlaceType getPlaceType(){
        return placeType;
    }

    public void setPlaceType(PlaceType placeType){
        this.placeType = placeType;
    }
}