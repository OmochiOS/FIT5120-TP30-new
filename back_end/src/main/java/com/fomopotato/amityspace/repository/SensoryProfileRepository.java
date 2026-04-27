package com.fomopotato.amityspace.repository;

import com.fomopotato.amityspace.entity.SensoryProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensoryProfileRepository extends JpaRepository<SensoryProfileEntity, Long> {
}