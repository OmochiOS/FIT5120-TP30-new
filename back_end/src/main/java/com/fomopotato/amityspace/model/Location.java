package com.fomopotato.amityspace.model;

import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;

public class Location {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private CrowdLevel crowdLevel;
    private String statusLabel;
    private SensoryProfile sensoryProfile;
    private PlaceType placeType;

    public Location(Long id, String name, double latitude, 
                    double longitude, CrowdLevel crowdLevel, 
                    String statusLabel, SensoryProfile sensoryProfile,
                    PlaceType placeType){
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.crowdLevel = crowdLevel;
        this.sensoryProfile = sensoryProfile;
        this.statusLabel = statusLabel;
        this.placeType = placeType;
    }

    public Location() {

    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public CrowdLevel getCrowdLevel(){
        return crowdLevel;
    }

    public void setCrowdLevel(CrowdLevel crowdLevel){
        this.crowdLevel = crowdLevel;
    }

    public String getStatusLabel(){
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel){
        this.statusLabel = statusLabel;
    }

    public SensoryProfile getSensoryProfile(){
        return sensoryProfile;
    }

    public void setSensoryProfile(SensoryProfile sensoryProfile){
        this.sensoryProfile = sensoryProfile;
    }

    public PlaceType getPlaceType(){
        return placeType;
    }

    public void setPlaceType(PlaceType placeType){
        this.placeType = placeType;
    }
}
