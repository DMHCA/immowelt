package com.romantrippel.immowelt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class EstateResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EstateDto(
            String headline,
            String globalObjectKey,
            String estateType,
            String exposeUrl,
            double livingArea,
            String image,
            String imageHD,
            String city,
            String zip,
            boolean showMap,
            String street,
            String priceName,
            String priceValue,
            int rooms
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EstateListData(List<EstateDto> data) {}

    public record Data(EstateListData estateList) {}

    public record Root(Data data) {}

    public static Root fromJson(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Root.class);
    }
}
