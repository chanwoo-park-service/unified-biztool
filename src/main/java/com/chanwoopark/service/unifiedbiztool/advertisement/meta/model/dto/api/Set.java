package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaAdStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Set {
    private String id;
    private String name;
    private MetaAdStatus status;
    @JsonProperty("effective_status")
    private String effectiveStatus;
    @JsonProperty("start_time")
    private String startTime;
}
