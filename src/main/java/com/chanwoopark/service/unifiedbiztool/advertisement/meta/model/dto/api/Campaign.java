package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaAdStatus;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignObjective;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Campaign {
    private String id;
    private String name;
    private MetaAdStatus status;
    private MetaCampaignObjective objective;
    @JsonProperty("effective_status")
    private String effectiveStatus;
    @JsonProperty("start_time")
    private String startTime;
}
