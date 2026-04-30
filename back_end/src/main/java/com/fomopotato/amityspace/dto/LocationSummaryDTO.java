package com.fomopotato.amityspace.dto;

import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;

public class LocationSummaryDTO {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private CrowdLevel crowdLevel;
    private String statusLabel;
    private PlaceType placeType;
    private String description;
    private boolean matchesPreferences; 

    public LocationSummaryDTO() {
    }

    public LocationSummaryDTO(Long id, String name, double latitude, double longitude,
                              CrowdLevel crowdLevel, String statusLabel, PlaceType placeType, String description, boolean matchesPreferences) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.crowdLevel = crowdLevel;
        this.statusLabel = statusLabel;
        this.matchesPreferences = matchesPreferences;
        this.description = description;
        this.placeType = placeType;
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

    public boolean getMatchesPreferences(){
        return matchesPreferences;
    }

    public void setMatchesPreferences(boolean matchedPreferences){
        this.matchesPreferences = matchedPreferences;
    }

    public PlaceType getPlaceType(){
        return placeType;
    }

    public void setPlaceType(PlaceType placeType){
        this.placeType = placeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}