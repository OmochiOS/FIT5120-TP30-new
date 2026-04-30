package com.fomopotato.amityspace.init;

import com.fomopotato.amityspace.service.sync.HistoricalCrowdProfileSyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class HistoricalCrowdProfileSyncRunner implements CommandLineRunner {

    private final HistoricalCrowdProfileSyncService historicalCrowdProfileSyncService;

    public HistoricalCrowdProfileSyncRunner(HistoricalCrowdProfileSyncService historicalCrowdProfileSyncService) {
        this.historicalCrowdProfileSyncService = historicalCrowdProfileSyncService;
    }

    @Override
    public void run(String... args) {
        int updatedCount = historicalCrowdProfileSyncService.syncHistoricalCrowdProfiles();
        System.out.println("Historical crowd profile sync completed. Updated profiles: " + updatedCount);
    }
}