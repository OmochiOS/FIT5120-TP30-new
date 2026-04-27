package com.fomopotato.amityspace.dto;

import com.fomopotato.amityspace.enums.PlaceType;

public class PreferenceRequestDTO {
    private Boolean lowCrowd;
    private Boolean lowNoise;
    private Boolean lowLighting;
    private Boolean quietSpaceRequired;
    private Boolean predictableEnvironment;
    private PlaceType placeType;

    public PreferenceRequestDTO() {
    }

    public PreferenceRequestDTO(Boolean lowCrowd, Boolean lowNoise,  Boolean lowLighting, Boolean quietSpaceRequired, Boolean predictableEnvironment, PlaceType placeType) {
        this.lowCrowd = lowCrowd;
        this.lowNoise = lowNoise;
        this.lowLighting = lowLighting;
        this.quietSpaceRequired = quietSpaceRequired;
        this.predictableEnvironment = predictableEnvironment;
        this.placeType = placeType;
    }

    public Boolean getLowCrowd() {
        return lowCrowd;
    }

    public void setLowCrowd(Boolean lowCrowd) {
        this.lowCrowd = lowCrowd;
    }

    public Boolean getLowNoise() {
        return lowNoise;
    }

    public void setLowNoise(Boolean lowNoise) {
        this.lowNoise = lowNoise;
    }

        public Boolean getLowLighting() {
        return lowLighting;
    }

    public void setLowLighting(Boolean lowLighting) {
        this.lowLighting = lowLighting;
    }

    public Boolean getQuietSpaceRequired() {
        return quietSpaceRequired;
    }

    public void setQuietSpaceRequired(Boolean quietSpaceRequired) {
        this.quietSpaceRequired = quietSpaceRequired;
    }

    public Boolean getPredictableEnvironment() {
        return predictableEnvironment;
    }

    public void setPredictableEnvironment(Boolean predictableEnvironment) {
        this.predictableEnvironment = predictableEnvironment;
    }
    public PlaceType getPlaceType(){
        return placeType;
    }

    public void setPlaceType(PlaceType placeType){
        this.placeType = placeType;
    }
}