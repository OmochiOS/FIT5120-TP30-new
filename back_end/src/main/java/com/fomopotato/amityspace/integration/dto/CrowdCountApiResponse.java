package com.fomopotato.amityspace.integration.dto;

import java.util.List;

public class CrowdCountApiResponse {

    private List<CrowdCountRecordDTO> results;

    public List<CrowdCountRecordDTO> getResults() {
        return results;
    }

    public void setResults(List<CrowdCountRecordDTO> results) {
        this.results = results;
    }
}