package com.fomopotato.amityspace.init;

import com.fomopotato.amityspace.service.sync.CrowdDataSyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CrowdDataSyncRunner implements CommandLineRunner {

    private final CrowdDataSyncService crowdDataSyncService;

    public CrowdDataSyncRunner(CrowdDataSyncService crowdDataSyncService) {
        this.crowdDataSyncService = crowdDataSyncService;
    }

    @Override
    public void run(String... args) {
        int updatedCount = crowdDataSyncService.syncRecentCrowdLevels();
        System.out.println("Crowd data sync completed. Updated locations: " + updatedCount);
    }
}