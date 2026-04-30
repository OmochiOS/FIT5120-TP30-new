package com.fomopotato.amityspace.integration.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CrowdCountRecordDTO {

    @JsonAlias({"location_id", "Location_ID"})
    private Long locationId;

    @JsonAlias({"total_of_directions", "Total_of_Directions"})
    private Integer totalOfDirections;

    public Long getLocationId() {
        return locationId;
    }

    public Integer getTotalOfDirections() {
        return totalOfDirections;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setTotalOfDirections(Integer totalOfDirections) {
        this.totalOfDirections = totalOfDirections;
    }
}