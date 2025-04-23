package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaAdStatus;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaBillingEvent;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaOptimizationGoal;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Set {

    private String id;

    private String name;

    @JsonProperty("special_ad_categories")
    private MetaOptimizationGoal optimizationGoal;

    @JsonProperty("billing_event")
    private MetaBillingEvent billingEvent;

    @JsonProperty("bid_amount")
    private Long bidAmount;

    @JsonProperty("campaign_id")
    private String campaignId;

    private Targeting targeting;

    @JsonProperty("daily_budget")
    private Long dailyBudget;

    private MetaAdStatus status;

    @JsonProperty("start_time")
    private String startTime;
}
