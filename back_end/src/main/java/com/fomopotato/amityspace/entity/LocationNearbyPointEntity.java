package com.fomopotato.amityspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "location_nearby_points")
public class LocationNearbyPointEntity {

    @Id
    private Long id;

    @Column(name = "source_location_id")
    private Long sourceLocationId;

    @Column(name = "nearby_location_id")
    private Long nearbyLocationId;

    @Column(name = "distance_meters")
    private Double distanceMeters;

    public LocationNearbyPointEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getSourceLocationId() {
        return sourceLocationId;
    }

    public Long getNearbyLocationId() {
        return nearbyLocationId;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceLocationId(Long sourceLocationId) {
        this.sourceLocationId = sourceLocationId;
    }

    public void setNearbyLocationId(Long nearbyLocationId) {
        this.nearbyLocationId = nearbyLocationId;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
}