package com.fomopotato.amityspace.data.record;

public class LocationHistoricalCrowdPatternRecord {

    private Long locationId;
    private String locationName;
    private String dayType;
    private String dayOfWeek;
    private String timeBlock;
    private Integer avgCrowdCount;
    private Integer minCrowdCount;
    private Integer maxCrowdCount;
    private String crowdLevelLabel;
    private String predictabilityLevel;
    private Boolean recommendedVisitFlag;
    private Integer sampleSize;
    private String dateRangeStart;
    private String dateRangeEnd;
    private String updatedAt;
    private String notes;

    public LocationHistoricalCrowdPatternRecord() {
    }

    public LocationHistoricalCrowdPatternRecord(Long locationId,
                                                String locationName,
                                                String dayType,
                                                String dayOfWeek,
                                                String timeBlock,
                                                Integer avgCrowdCount,
                                                Integer minCrowdCount,
                                                Integer maxCrowdCount,
                                                String crowdLevelLabel,
                                                String predictabilityLevel,
                                                Boolean recommendedVisitFlag,
                                                Integer sampleSize,
                                                String dateRangeStart,
                                                String dateRangeEnd,
                                                String updatedAt,
                                                String notes) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.dayType = dayType;
        this.dayOfWeek = dayOfWeek;
        this.timeBlock = timeBlock;
        this.avgCrowdCount = avgCrowdCount;
        this.minCrowdCount = minCrowdCount;
        this.maxCrowdCount = maxCrowdCount;
        this.crowdLevelLabel = crowdLevelLabel;
        this.predictabilityLevel = predictabilityLevel;
        this.recommendedVisitFlag = recommendedVisitFlag;
        this.sampleSize = sampleSize;
        this.dateRangeStart = dateRangeStart;
        this.dateRangeEnd = dateRangeEnd;
        this.updatedAt = updatedAt;
        this.notes = notes;
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

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeBlock() {
        return timeBlock;
    }

    public void setTimeBlock(String timeBlock) {
        this.timeBlock = timeBlock;
    }

    public Integer getAvgCrowdCount() {
        return avgCrowdCount;
    }

    public void setAvgCrowdCount(Integer avgCrowdCount) {
        this.avgCrowdCount = avgCrowdCount;
    }

    public Integer getMinCrowdCount() {
        return minCrowdCount;
    }

    public void setMinCrowdCount(Integer minCrowdCount) {
        this.minCrowdCount = minCrowdCount;
    }

    public Integer getMaxCrowdCount() {
        return maxCrowdCount;
    }

    public void setMaxCrowdCount(Integer maxCrowdCount) {
        this.maxCrowdCount = maxCrowdCount;
    }

    public String getCrowdLevelLabel() {
        return crowdLevelLabel;
    }

    public void setCrowdLevelLabel(String crowdLevelLabel) {
        this.crowdLevelLabel = crowdLevelLabel;
    }

    public String getPredictabilityLevel() {
        return predictabilityLevel;
    }

    public void setPredictabilityLevel(String predictabilityLevel) {
        this.predictabilityLevel = predictabilityLevel;
    }

    public Boolean getRecommendedVisitFlag() {
        return recommendedVisitFlag;
    }

    public void setRecommendedVisitFlag(Boolean recommendedVisitFlag) {
        this.recommendedVisitFlag = recommendedVisitFlag;
    }

    public Integer getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(Integer sampleSize) {
        this.sampleSize = sampleSize;
    }

    public String getDateRangeStart() {
        return dateRangeStart;
    }

    public void setDateRangeStart(String dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    public String getDateRangeEnd() {
        return dateRangeEnd;
    }

    public void setDateRangeEnd(String dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}