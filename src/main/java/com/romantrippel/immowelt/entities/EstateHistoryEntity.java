package com.romantrippel.immowelt.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "estate_history")
@Data
@AllArgsConstructor
@Builder
public class EstateHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "estate_id", nullable = false)
  private EstateEntity estate;

  @Column(nullable = false)
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
  private String availability;

  @Column(updatable = false)
  private LocalDateTime createdAt;

  protected EstateHistoryEntity() {}

  public static EstateHistoryEntity fromEstate(EstateEntity estate) {
    return EstateHistoryEntity.builder()
        .estate(estate)
        .globalObjectKey(estate.getGlobalObjectKey())
        .headline(estate.getHeadline())
        .estateType(estate.getEstateType())
        .exposeUrl(estate.getExposeUrl())
        .livingArea(estate.getLivingArea())
        .image(estate.getImage())
        .imageHD(estate.getImageHD())
        .city(estate.getCity())
        .zip(estate.getZip())
        .showMap(estate.isShowMap())
        .street(estate.getStreet())
        .priceName(estate.getPriceName())
        .priceValue(estate.getPriceValue())
        .rooms(estate.getRooms())
        .apartmentLayoutUrl(estate.getApartmentLayoutUrl())
        .createdAt(estate.getCreatedAt())
        .availability(estate.getAvailability())
        .build();
  }
}
