package com.romantrippel.immowelt.repositories;

import com.romantrippel.immowelt.entities.EstateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EstateRepository extends JpaRepository<EstateEntity, String> {

  @Transactional
  @Modifying
  @Query(
      value =
          """
      INSERT INTO estates (
          global_object_key, headline, estate_type, expose_url, living_area,
          image, imagehd, city, zip, show_map,
          street, price_name, price_value, rooms, created_at
      ) VALUES (
          :#{#e.globalObjectKey}, :#{#e.headline}, :#{#e.estateType}, :#{#e.exposeUrl}, :#{#e.livingArea},
          :#{#e.image}, :#{#e.imageHD}, :#{#e.city}, :#{#e.zip}, :#{#e.showMap},
          :#{#e.street}, :#{#e.priceName}, :#{#e.priceValue}, :#{#e.rooms}, :#{#e.createdAt}
      )
      ON CONFLICT (global_object_key) DO NOTHING
      """,
      nativeQuery = true)
  int insertIfNotExists(@Param("e") EstateEntity e);
}
