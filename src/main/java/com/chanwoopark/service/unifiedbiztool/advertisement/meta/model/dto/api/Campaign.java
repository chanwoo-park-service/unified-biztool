package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaAdStatus;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignObjective;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Campaign {
    private String id;
    private String name;
    private MetaCampaignObjective objective;
    private MetaAdStatus status;
    @JsonProperty("special_ad_categories")
    private List<String> specialAdCategories;
    @JsonProperty("start_time")
    private String startTime;
    @JsonProperty("daily_budget")
    private Long dailyBudget;
}
