package com.fomopotato.amityspace.service.sync;

import com.fomopotato.amityspace.entity.LocationEntity;
import com.fomopotato.amityspace.enums.CrowdLevel;
import com.fomopotato.amityspace.integration.client.MelbourneCrowdApiClient;
import com.fomopotato.amityspace.integration.dto.CrowdCountApiResponse;
import com.fomopotato.amityspace.integration.dto.CrowdCountRecordDTO;
import com.fomopotato.amityspace.repository.LocationRepository;
import com.fomopotato.amityspace.service.calculator.CrowdLevelCalculator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CrowdDataSyncService {

    private final MelbourneCrowdApiClient crowdApiClient;
    private final LocationRepository locationRepository;
    private final CrowdLevelCalculator crowdLevelCalculator;

    public CrowdDataSyncService(MelbourneCrowdApiClient crowdApiClient,
                                LocationRepository locationRepository,
                                CrowdLevelCalculator crowdLevelCalculator) {
        this.crowdApiClient = crowdApiClient;
        this.locationRepository = locationRepository;
        this.crowdLevelCalculator = crowdLevelCalculator;
    }

    public int syncRecentCrowdLevels() {
        CrowdCountApiResponse response = crowdApiClient.fetchRecentCrowdCounts();

        if (response == null || response.getResults() == null) {
            return 0;
        }

        Map<Long, Integer> totalCountByLocationId = new HashMap<>();

        for (CrowdCountRecordDTO record : response.getResults()) {
            if (record.getLocationId() == null || record.getTotalOfDirections() == null) {
                continue;
            }

            totalCountByLocationId.merge(
                    record.getLocationId(),
                    record.getTotalOfDirections(),
                    Integer::sum
            );
        }

        int updatedCount = 0;

        for (Map.Entry<Long, Integer> entry : totalCountByLocationId.entrySet()) {
            Long locationId = entry.getKey();
            Integer recentTotalCount = entry.getValue();

            Optional<LocationEntity> optionalLocation = locationRepository.findById(locationId);

            if (optionalLocation.isEmpty()) {
                continue;
            }

            LocationEntity location = optionalLocation.get();

            CrowdLevel crowdLevel = crowdLevelCalculator.resolveCrowdLevel(recentTotalCount);

            location.setCrowdLevel(crowdLevel);
            location.setStatusLabel(crowdLevelCalculator.buildStatusLabel(crowdLevel));

            locationRepository.save(location);
            updatedCount++;
        }

        return updatedCount;
    }
}