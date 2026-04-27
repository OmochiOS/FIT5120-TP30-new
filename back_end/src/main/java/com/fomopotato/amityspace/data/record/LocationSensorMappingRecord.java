package com.fomopotato.amityspace.data.record;

public class LocationSensorMappingRecord {

    private Long locationId;
    private String locationName;
    private Long crowdSensorLocationId;
    private String noiseSensorIdOrName;
    private Double noiseSensorLat;
    private Double noiseSensorLng;
    private String mappingMethod;
    private String mappingConfidence;
    private String notes;

    public LocationSensorMappingRecord() {
    }

    public LocationSensorMappingRecord(Long locationId,
                                       String locationName,
                                       Long crowdSensorLocationId,
                                       String noiseSensorIdOrName,
                                       Double noiseSensorLat,
                                       Double noiseSensorLng,
                                       String mappingMethod,
                                       String mappingConfidence,
                                       String notes) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.crowdSensorLocationId = crowdSensorLocationId;
        this.noiseSensorIdOrName = noiseSensorIdOrName;
        this.noiseSensorLat = noiseSensorLat;
        this.noiseSensorLng = noiseSensorLng;
        this.mappingMethod = mappingMethod;
        this.mappingConfidence = mappingConfidence;
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

    public Long getCrowdSensorLocationId() {
        return crowdSensorLocationId;
    }

    public void setCrowdSensorLocationId(Long crowdSensorLocationId) {
        this.crowdSensorLocationId = crowdSensorLocationId;
    }

    public String getNoiseSensorIdOrName() {
        return noiseSensorIdOrName;
    }

    public void setNoiseSensorIdOrName(String noiseSensorIdOrName) {
        this.noiseSensorIdOrName = noiseSensorIdOrName;
    }

    public Double getNoiseSensorLat() {
        return noiseSensorLat;
    }

    public void setNoiseSensorLat(Double noiseSensorLat) {
        this.noiseSensorLat = noiseSensorLat;
    }

    public Double getNoiseSensorLng() {
        return noiseSensorLng;
    }

    public void setNoiseSensorLng(Double noiseSensorLng) {
        this.noiseSensorLng = noiseSensorLng;
    }

    public String getMappingMethod() {
        return mappingMethod;
    }

    public void setMappingMethod(String mappingMethod) {
        this.mappingMethod = mappingMethod;
    }

    public String getMappingConfidence() {
        return mappingConfidence;
    }

    public void setMappingConfidence(String mappingConfidence) {
        this.mappingConfidence = mappingConfidence;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}