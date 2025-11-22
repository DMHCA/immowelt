package com.romantrippel.immowelt.repositories;

import com.romantrippel.immowelt.entities.EstateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstateRepository extends JpaRepository<EstateEntity, Long> {

  Optional<EstateEntity> findByGlobalObjectKey(String globalObjectKey);
}
