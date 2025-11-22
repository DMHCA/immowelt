package com.romantrippel.immowelt.repositories;

import com.romantrippel.immowelt.entities.EstateHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstateHistoryRepository extends JpaRepository<EstateHistoryEntity, Long> {}
