package com.fomopotato.amityspace.entity;

import com.fomopotato.amityspace.enums.PredictabilityLevel;
import com.fomopotato.amityspace.enums.SensoryLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensory_profiles")
public class SensoryProfileEntity {

    @Id
    @Column(name = "location_id")
    private Long locationId;

    private Double noiseDb;

    @Enumerated(EnumType.STRING)
    private SensoryLevel noiseLevel;

    @Enumerated(EnumType.STRING)
    private SensoryLevel lightingLevel;

    @Enumerated(EnumType.STRING)
    private PredictabilityLevel predictabilityLevel;

    private Boolean quietSpaceAvailable;

    private String overallSensoryScore;
    private String bestTimesToVisit;

    @Column(length = 1000)
    private String goodToKnowTips;

    private String lastUpdated;

    @Column(length = 1000)
    private String summary;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    public SensoryProfileEntity() {
    }

    public Long getLocationId() {
        return locationId;
    }

    public Double getNoiseDb() {
        return noiseDb;
    }

    public SensoryLevel getNoiseLevel() {
        return noiseLevel;
    }

    public SensoryLevel getLightingLevel() {
        return lightingLevel;
    }

    public PredictabilityLevel getPredictabilityLevel() {
        return predictabilityLevel;
    }

    public Boolean getQuietSpaceAvailable() {
        return quietSpaceAvailable;
    }

    public String getOverallSensoryScore() {
        return overallSensoryScore;
    }

    public String getBestTimesToVisit() {
        return bestTimesToVisit;
    }

    public String getGoodToKnowTips() {
        return goodToKnowTips;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getSummary() {
        return summary;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setNoiseDb(Double noiseDb) {
        this.noiseDb = noiseDb;
    }

    public void setNoiseLevel(SensoryLevel noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public void setLightingLevel(SensoryLevel lightingLevel) {
        this.lightingLevel = lightingLevel;
    }

    public void setPredictabilityLevel(PredictabilityLevel predictabilityLevel) {
        this.predictabilityLevel = predictabilityLevel;
    }

    public void setQuietSpaceAvailable(Boolean quietSpaceAvailable) {
        this.quietSpaceAvailable = quietSpaceAvailable;
    }

    public void setOverallSensoryScore(String overallSensoryScore) {
        this.overallSensoryScore = overallSensoryScore;
    }

    public void setBestTimesToVisit(String bestTimesToVisit) {
        this.bestTimesToVisit = bestTimesToVisit;
    }

    public void setGoodToKnowTips(String goodToKnowTips) {
        this.goodToKnowTips = goodToKnowTips;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }
}