package com.romantrippel.immowelt.repositories;

import com.romantrippel.immowelt.entities.EstateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface EstateRepository extends JpaRepository<EstateEntity, String> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO estates (
            global_object_key, headline, estate_type, expose_url, living_area,
            image, imagehd, city, zip, show_map,
            street, price_name, price_value, rooms, created_at
        ) VALUES (
            :globalObjectKey, :headline, :estateType, :exposeUrl, :livingArea,
            :image, :imageHD, :city, :zip, :showMap,
            :street, :priceName, :priceValue, :rooms, :createdAt
        )
        ON CONFLICT (global_object_key) DO NOTHING
        """, nativeQuery = true)
    int insertIfNotExists(
            @Param("globalObjectKey") String globalObjectKey,
            @Param("headline") String headline,
            @Param("estateType") String estateType,
            @Param("exposeUrl") String exposeUrl,
            @Param("livingArea") double livingArea,
            @Param("image") String image,
            @Param("imageHD") String imageHD,
            @Param("city") String city,
            @Param("zip") String zip,
            @Param("showMap") boolean showMap,
            @Param("street") String street,
            @Param("priceName") String priceName,
            @Param("priceValue") String priceValue,
            @Param("rooms") int rooms,
            @Param("createdAt") LocalDateTime createdAt
    );

}