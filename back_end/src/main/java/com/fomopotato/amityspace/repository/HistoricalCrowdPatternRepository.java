package com.fomopotato.amityspace.repository;

import com.fomopotato.amityspace.entity.HistoricalCrowdPatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricalCrowdPatternRepository extends JpaRepository<HistoricalCrowdPatternEntity, Long> {

    List<HistoricalCrowdPatternEntity> findByLocationId(Long locationId);

    List<HistoricalCrowdPatternEntity> findByLocationIdAndHour(Long locationId, Integer hour);
}