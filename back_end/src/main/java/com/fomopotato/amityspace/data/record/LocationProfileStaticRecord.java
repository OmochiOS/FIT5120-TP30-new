package com.fomopotato.amityspace.data.record;

public class LocationProfileStaticRecord {

    private Long locationId;
    private String locationName;
    private String placeType;
    private String lightingLevel;
    private Boolean quietSpaceAvailable;
    private String overallSensoryScore;
    private String summary;
    private String goodToKnowTips;
    private String escapeRouteNotes;
    private String bestTimesToVisit;
    private String lastReviewed;
    private String source;
    private String confidence;

    public LocationProfileStaticRecord() {
    }

    public LocationProfileStaticRecord(Long locationId,
                                       String locationName,
                                       String placeType,
                                       String lightingLevel,
                                       Boolean quietSpaceAvailable,
                                       String overallSensoryScore,
                                       String summary,
                                       String goodToKnowTips,
                                       String escapeRouteNotes,
                                       String bestTimesToVisit,
                                       String lastReviewed,
                                       String source,
                                       String confidence) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.placeType = placeType;
        this.lightingLevel = lightingLevel;
        this.quietSpaceAvailable = quietSpaceAvailable;
        this.overallSensoryScore = overallSensoryScore;
        this.summary = summary;
        this.goodToKnowTips = goodToKnowTips;
        this.escapeRouteNotes = escapeRouteNotes;
        this.bestTimesToVisit = bestTimesToVisit;
        this.lastReviewed = lastReviewed;
        this.source = source;
        this.confidence = confidence;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getLightingLevel() {
        return lightingLevel;
    }

    public void setLightingLevel(String lightingLevel) {
        this.lightingLevel = lightingLevel;
    }

    public Boolean getQuietSpaceAvailable() {
        return quietSpaceAvailable;
    }

    public void setQuietSpaceAvailable(Boolean quietSpaceAvailable) {
        this.quietSpaceAvailable = quietSpaceAvailable;
    }

    public String getOverallSensoryScore() {
        return overallSensoryScore;
    }

    public void setOverallSensoryScore(String overallSensoryScore) {
        this.overallSensoryScore = overallSensoryScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getGoodToKnowTips() {
        return goodToKnowTips;
    }

    public void setGoodToKnowTips(String goodToKnowTips) {
        this.goodToKnowTips = goodToKnowTips;
    }

    public String getEscapeRouteNotes() {
        return escapeRouteNotes;
    }

    public void setEscapeRouteNotes(String escapeRouteNotes) {
        this.escapeRouteNotes = escapeRouteNotes;
    }

    public String getBestTimesToVisit() {
        return bestTimesToVisit;
    }

    public void setBestTimesToVisit(String bestTimesToVisit) {
        this.bestTimesToVisit = bestTimesToVisit;
    }

    public String getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(String lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}