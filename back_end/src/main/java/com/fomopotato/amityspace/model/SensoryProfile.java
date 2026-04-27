package com.fomopotato.amityspace.model;

import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;

public class SensoryProfile {

    private Long locationId;
    private Double noiseDb;
    private SensoryLevel noiseLevel;
    private SensoryLevel lightingLevel;
    private PredictabilityLevel predictabilityLevel;
    private boolean quietSpaceAvailable;
    private String overallSensoryScore;
    private String bestTimesToVisit;
    private String goodToKnowTips;
    private String lastUpdated;
    private String summary;

    public SensoryProfile(Long locationId, Double noiseDb, SensoryLevel noiseLevel,
                          SensoryLevel lightingLevel, PredictabilityLevel predictabilityLevel,
                          boolean quietSpaceAvailable, String overallSensoryScore,
                          String bestTimeToVisit, String goodToKnowTips, 
                          String lastUpdated, String summary) {
        this.locationId = locationId;
        this.noiseDb = noiseDb;
        this.noiseLevel = noiseLevel;
        this.lightingLevel = lightingLevel;
        this.predictabilityLevel = predictabilityLevel;
        this.quietSpaceAvailable = quietSpaceAvailable;
        this.overallSensoryScore = overallSensoryScore;
        this.bestTimesToVisit = bestTimeToVisit;
        this.goodToKnowTips = goodToKnowTips;
        this.lastUpdated = lastUpdated;
        this.summary = summary;
    }

    public SensoryProfile() {
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Double getNoiseDb() {
        return noiseDb;
    }

    public void setNoiseDb(Double noiseDb) {
        this.noiseDb = noiseDb;
    }

    public SensoryLevel getNoiseLevel() {
        return noiseLevel;
    }

    public void setNoiseLevel(SensoryLevel noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public SensoryLevel getLightingLevel() {
        return lightingLevel;
    }

    public void setLightingLevel(SensoryLevel lightingLevel) {
        this.lightingLevel = lightingLevel;
    }

    public PredictabilityLevel getPredictabilityLevel() {
        return predictabilityLevel;
    }

    public void setPredictabilityLevel(PredictabilityLevel predictabilityLevel) {
        this.predictabilityLevel = predictabilityLevel;
    }

    public boolean isQuietSpaceAvailable() {
        return quietSpaceAvailable;
    }

    public void setQuietSpaceAvailable(boolean quietSpaceAvailable) {
        this.quietSpaceAvailable = quietSpaceAvailable;
    }

    public String getOverallSensoryScore() {
        return overallSensoryScore;
    }

    public void setOverallSensoryScore(String overallSensoryScore) {
        this.overallSensoryScore = overallSensoryScore;
    }

    public String getBestTimesToVisit() {
        return bestTimesToVisit;
    }

    public void setBestTimesToVisit(String bestTimesToVisit) {
        this.bestTimesToVisit = bestTimesToVisit;
    }

    public String getGoodToKnowTips() {
        return goodToKnowTips;
    }

    public void setGoodToKnowTips(String goodToKnowTips) {
        this.goodToKnowTips = goodToKnowTips;
    }

    public String getLastUpdated(){
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated){
        this.lastUpdated = lastUpdated;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}