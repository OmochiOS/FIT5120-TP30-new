package com.fomopotato.amityspace.service.calculator;

import com.fomopotato.amityspace.data.record.LocationSensorMappingRecord;
import com.fomopotato.amityspace.enums.SensoryLevel;
import com.fomopotato.amityspace.integration.dto.NoiseApiResponse;
import com.fomopotato.amityspace.model.Location;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoiseCalculator {

    public Double resolveNoiseDb(Location location,
                                 LocationSensorMappingRecord mappingRecord,
                                 List<NoiseApiResponse> noiseRecords) {
        Double mappedNoise = resolveMappedNoiseDb(mappingRecord, noiseRecords);
        if (mappedNoise != null) {
            return mappedNoise;
        }

        return resolveNearestNoiseDb(location, noiseRecords);
    }

    public Double resolveMappedNoiseDb(LocationSensorMappingRecord mappingRecord,
                                       List<NoiseApiResponse> noiseRecords) {
        NoiseApiResponse best = null;
        double bestDistance = Double.MAX_VALUE;

        if (mappingRecord == null || noiseRecords == null || noiseRecords.isEmpty()) {
            return null;
        }

        Double mappingLat = mappingRecord.getNoiseSensorLat();
        Double mappingLng = mappingRecord.getNoiseSensorLng();

        if (mappingLat == null || mappingLng == null) {
            return null;
        }

        for (NoiseApiResponse record : noiseRecords) {
            double distance = squaredDistance(
                    mappingLat,
                    mappingLng,
                    record.getLatitude(),
                    record.getLongitude()
            );

            if (distance < bestDistance) {
                bestDistance = distance;
                best = record;
            }
        }

        if (best == null || bestDistance > 0.0001) {
            return null;
        }

        return best.getNoiseDb();
    }

    public Double resolveNearestNoiseDb(Location location, List<NoiseApiResponse> noiseRecords) {
        NoiseApiResponse best = null;
        double bestDistance = Double.MAX_VALUE;

        if (location == null || noiseRecords == null || noiseRecords.isEmpty()) {
            return null;
        }

        for (NoiseApiResponse record : noiseRecords) {
            double distance = squaredDistance(
                    location.getLatitude(),
                    location.getLongitude(),
                    record.getLatitude(),
                    record.getLongitude()
            );

            if (distance < bestDistance) {
                bestDistance = distance;
                best = record;
            }
        }

        if (best == null || bestDistance > 0.01) {
            return null;
        }

        return best.getNoiseDb();
    }

    public SensoryLevel mapNoiseLevel(Double noiseDb) {
        if (noiseDb == null) {
            return null;
        }

        if (noiseDb < 45) {
            return SensoryLevel.LOW;
        }

        if (noiseDb < 60) {
            return SensoryLevel.MODERATE;
        }

        if (noiseDb < 70) {
            return SensoryLevel.HIGH;
        }

        return SensoryLevel.OVERSTIMULATING;
    }

    private double squaredDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = lat1 - lat2;
        double dLng = lng1 - lng2;
        return dLat * dLat + dLng * dLng;
    }
}