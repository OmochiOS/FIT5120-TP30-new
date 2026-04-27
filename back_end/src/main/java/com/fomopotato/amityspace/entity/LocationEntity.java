package com.fomopotato.amityspace.entity;

import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.enums.PlaceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "locations")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private CrowdLevel crowdLevel;

    private String statusLabel;

    @Enumerated(EnumType.STRING)
    private PlaceType placeType;

    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SensoryProfileEntity sensoryProfile;

    public LocationEntity() {
    }

    public LocationEntity(Long id, String name, Double latitude, Double longitude,
                          CrowdLevel crowdLevel, String statusLabel, PlaceType placeType) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.crowdLevel = crowdLevel;
        this.statusLabel = statusLabel;
        this.placeType = placeType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public CrowdLevel getCrowdLevel() {
        return crowdLevel;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public SensoryProfileEntity getSensoryProfile() {
        return sensoryProfile;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setCrowdLevel(CrowdLevel crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }

    public void setSensoryProfile(SensoryProfileEntity sensoryProfile) {
        this.sensoryProfile = sensoryProfile;
    }
}