package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Targeting {
    @JsonProperty("geo_locations")
    private GeoLocations geoLocations;

    @Setter
    @JsonProperty("age_min")
    private Integer ageMin;

    @Setter
    @JsonProperty("age_max")
    private String ageMax;

    @JsonProperty("genders")
    private List<Integer> genders;

    @JsonProperty("locales")
    private List<Integer> locales;

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    @Builder
    public static class GeoLocations {
        private List<String> countries;
    }
}