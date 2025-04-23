package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Pixel {
    private String id;
    private String name;
    @JsonProperty("creation_time")
    private String creationTime;
}
