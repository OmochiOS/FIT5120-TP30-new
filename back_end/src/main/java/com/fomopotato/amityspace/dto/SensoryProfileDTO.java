package com.fomopotato.amityspace.dto;

public class SensoryProfileDTO {
    private Double noiseDb;
    private String noiseLevel;
    private String noiseDescription;
    private String lightingLevel;
    private String lightingDescription;
    private String predictabilityLevel;
    private String predictabilityDescription;
    private Boolean quietSpaceAvailable;
    private String quietSpaceDescription;
    private String overallSensoryScore;
    private String bestTimesToVisit;
    private String goodToKnowTips;
    private String lastUpdated;


    public SensoryProfileDTO() {
    }

    public SensoryProfileDTO(Double noiseDb,
                             String noiseLevel,
                             String noiseDescription,
                             String lightingLevel,
                             String lightingDescription,
                             String predictabilityLevel,
                             String predictabilityDescription,
                             Boolean quietSpaceAvailable,
                             String quietSpaceDescription,
                             String overallSensoryScore,
                             String bestTimesToVisit,
                             String goodToKnowTips,
                             String lastUpdated) {
        this.noiseDb = noiseDb;
        this.noiseLevel = noiseLevel;
        this.noiseDescription = noiseDescription;
        this.lightingLevel = lightingLevel;
        this.lightingDescription = lightingDescription;
        this.predictabilityLevel = predictabilityLevel;
        this.predictabilityDescription = predictabilityDescription;
        this.quietSpaceAvailable = quietSpaceAvailable;
        this.quietSpaceDescription = quietSpaceDescription;
        this.overallSensoryScore = overallSensoryScore;
        this.bestTimesToVisit = bestTimesToVisit;
        this.goodToKnowTips = goodToKnowTips;
        this.lastUpdated = lastUpdated;
    }

    public Double getNoiseDb() {
        return noiseDb;
    }

    public void setNoiseDb(Double noiseDb) {
        this.noiseDb = noiseDb;
    }

    public String getNoiseLevel(){
        return noiseLevel;
    }

    public void setNoiseLevel(String noiseLevel){
        this.noiseLevel = noiseLevel;
    }

    public String getNoiseDescription() {
        return noiseDescription;
    }

    public void setNoiseDescription(String noiseDescription) {
        this.noiseDescription = noiseDescription;
    }

    public String getLightingLevel() {
        return lightingLevel;
    }

    public void setLightingLevel(String lightingLevel) {
        this.lightingLevel = lightingLevel;
    }

    public String getLightingDescription() {
        return lightingDescription;
    }

    public void setLightingDescription(String lightingDescription) {
        this.lightingDescription = lightingDescription;
    }

    public String getPredictabilityLevel() {
        return predictabilityLevel;
    }

    public void setPredictabilityLevel(String predictabilityLevel) {
        this.predictabilityLevel = predictabilityLevel;
    }

    public String getPredictabilityDescription() {
        return predictabilityDescription;
    }

    public void setPredictabilityDescription(String predictabilityDescription) {
        this.predictabilityDescription = predictabilityDescription;
    }

    public boolean isQuietSpaceAvailable() {
        return quietSpaceAvailable;
    }

    public void setQuietSpaceAvailable(Boolean quietSpaceAvailable) {
        this.quietSpaceAvailable = quietSpaceAvailable;
    }

    public String getQuietSpaceDescription() {
        return quietSpaceDescription;
    }

    public void setQuietSpaceDescription(String quietSpaceDescription) {
        this.quietSpaceDescription = quietSpaceDescription;
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
}