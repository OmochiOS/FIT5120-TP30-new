package com.fomopotato.amityspace.dto;

public class CrowdInsightDTO {

    private Long locationId;
    private Integer visitHour;
    private String visitTimeLabel;
    private String predictedCrowdLevel;
    private String suitabilityLabel;
    private String alternativeSuggestion;
    private Boolean dataAvailable;
    private String dataNote;

    public CrowdInsightDTO() {
    }

    public CrowdInsightDTO(Long locationId,
                           Integer visitHour,
                           String visitTimeLabel,
                           String predictedCrowdLevel,
                           String suitabilityLabel,
                           String alternativeSuggestion,
                           Boolean dataAvailable,
                           String dataNote) {
        this.locationId = locationId;
        this.visitHour = visitHour;
        this.visitTimeLabel = visitTimeLabel;
        this.predictedCrowdLevel = predictedCrowdLevel;
        this.suitabilityLabel = suitabilityLabel;
        this.alternativeSuggestion = alternativeSuggestion;
        this.dataAvailable = dataAvailable;
        this.dataNote = dataNote;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Integer getVisitHour() {
        return visitHour;
    }

    public String getVisitTimeLabel() {
        return visitTimeLabel;
    }

    public String getPredictedCrowdLevel() {
        return predictedCrowdLevel;
    }

    public String getSuitabilityLabel() {
        return suitabilityLabel;
    }

    public String getAlternativeSuggestion() {
        return alternativeSuggestion;
    }

    public Boolean getDataAvailable() {
        return dataAvailable;
    }

    public String getDataNote() {
        return dataNote;
    }
}