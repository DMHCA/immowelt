package com.romantrippel.immowelt.entities;

import com.romantrippel.immowelt.dto.EstateResponse.EstateDto;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "estates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EstateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
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
  private String apartmentLayoutUrl;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public static EstateEntity fromDto(EstateDto dto) {
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
