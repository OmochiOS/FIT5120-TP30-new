package com.fomopotato.amityspace.repository;

import com.fomopotato.amityspace.entity.LocationNearbyPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationNearbyPointRepository extends JpaRepository<LocationNearbyPointEntity, Long> {

    List<LocationNearbyPointEntity> findBySourceLocationIdOrderByDistanceMetersAsc(Long sourceLocationId);
}