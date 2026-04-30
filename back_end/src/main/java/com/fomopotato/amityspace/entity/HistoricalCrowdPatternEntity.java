package com.fomopotato.amityspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "historical_crowd_patterns")
public class HistoricalCrowdPatternEntity {

    @Id
    private Long id;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "crowd_sensor_id")
    private Long crowdSensorId;

    @Column(name = "day_type")
    private String dayType;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "hour")
    private Integer hour;

    @Column(name = "time_block")
    private String timeBlock;

    @Column(name = "avg_crowd_count")
    private Double avgCount;

    @Column(name = "min_crowd_count")
    private Integer minCount;

    @Column(name = "max_crowd_count")
    private Integer maxCount;

    @Column(name = "std_crowd_count")
    private Double stdCount;

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Column(name = "crowd_level_label")
    private String crowdLevelLabel;

    @Column(name = "predicted_predictability_level")
    private String predictabilityLevel;

    @Column(name = "recommended_visit_flag")
    private Boolean recommendedVisitFlag;

    @Column(name = "date_range_start")
    private String dateRangeStart;

    @Column(name = "date_range_end")
    private String dateRangeEnd;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "notes")
    private String notes;

    public HistoricalCrowdPatternEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public Long getCrowdSensorId() {
        return crowdSensorId;
    }

    public String getDayType() {
        return dayType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public Integer getHour() {
        return hour;
    }

    public String getTimeBlock() {
        return timeBlock;
    }

    public Double getAvgCount() {
        return avgCount;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public Double getStdCount() {
        return stdCount;
    }

    public Integer getSampleSize() {
        return sampleSize;
    }

    public String getCrowdLevelLabel() {
        return crowdLevelLabel;
    }

    public String getPredictabilityLevel() {
        return predictabilityLevel;
    }

    public Boolean getRecommendedVisitFlag() {
        return recommendedVisitFlag;
    }

    public String getDateRangeStart() {
        return dateRangeStart;
    }

    public String getDateRangeEnd() {
        return dateRangeEnd;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setCrowdSensorId(Long crowdSensorId) {
        this.crowdSensorId = crowdSensorId;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public void setTimeBlock(String timeBlock) {
        this.timeBlock = timeBlock;
    }

    public void setAvgCount(Double avgCount) {
        this.avgCount = avgCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public void setStdCount(Double stdCount) {
        this.stdCount = stdCount;
    }

    public void setSampleSize(Integer sampleSize) {
        this.sampleSize = sampleSize;
    }

    public void setCrowdLevelLabel(String crowdLevelLabel) {
        this.crowdLevelLabel = crowdLevelLabel;
    }

    public void setPredictabilityLevel(String predictabilityLevel) {
        this.predictabilityLevel = predictabilityLevel;
    }

    public void setRecommendedVisitFlag(Boolean recommendedVisitFlag) {
        this.recommendedVisitFlag = recommendedVisitFlag;
    }

    public void setDateRangeStart(String dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    public void setDateRangeEnd(String dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}