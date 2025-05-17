package com.romantrippel.immowelt.entities;


import com.romantrippel.immowelt.dto.EstateResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "estates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EstateEntity {

    @Id
    private String globalObjectKey;

    private String headline;
    private String estateType;
    private String exposeUrl;
    private double livingArea;
    private String image;
    private String imageHD;
    private String city;
    private String zip;
    private boolean showMap;
    private String street;
    private String priceName;
    private String priceValue;
    private int rooms;
    private LocalDateTime createdAt = LocalDateTime.now();

    public static EstateEntity fromDto(EstateResponse.EstateDto dto) {
        return EstateEntity.builder()
                .globalObjectKey(dto.globalObjectKey())
                .headline(dto.headline())
                .estateType(dto.estateType())
                .exposeUrl(dto.exposeUrl())
                .livingArea(dto.livingArea())
                .image(dto.image())
                .imageHD(dto.imageHD())
                .city(dto.city())
                .zip(dto.zip())
                .showMap(dto.showMap())
                .street(dto.street())
                .priceName(dto.priceName())
                .priceValue(dto.priceValue())
                .rooms(dto.rooms())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
