package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCustomEventType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class PromotedObject {

    @JsonProperty("pixel_id")
    private String pixelId;

    @JsonProperty("custom_event_type")
    private MetaCustomEventType metaCustomEventType;

}