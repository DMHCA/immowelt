package com.romantrippel.immowelt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EstateResponse {

  public record EstateDto(
      @NotBlank String headline,
      @NotBlank String globalObjectKey,
      @NotBlank String estateType,
      @NotBlank String exposeUrl,
      @PositiveOrZero double livingArea,
      String image,
      String imageHD,
      @NotBlank String city,
      @NotBlank String zip,
      boolean showMap,
      String street,
      String priceName,
      String priceValue,
      @PositiveOrZero int rooms) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record EstateListData(@NotNull List<@Valid EstateDto> data) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Data(@NotNull EstateListData estateList) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Root(@NotNull Data data) {}
}
