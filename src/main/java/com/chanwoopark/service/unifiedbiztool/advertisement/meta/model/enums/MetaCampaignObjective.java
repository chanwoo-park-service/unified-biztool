package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;

@Getter
public enum MetaCampaignObjective {
    APP_INSTALLS,
    BRAND_AWARENESS,
    EVENT_RESPONSES,
    LEAD_GENERATION,
    LINK_CLICKS,
    LOCAL_AWARENESS,
    MESSAGES,
    OFFER_CLAIMS,
    PAGE_LIKES,
    POST_ENGAGEMENT,
    PRODUCT_CATALOG_SALES,
    REACH,
    STORE_VISITS,
    VIDEO_VIEWS,

    OUTCOME_AWARENESS,
    OUTCOME_ENGAGEMENT,
    OUTCOME_LEADS,
    OUTCOME_SALES,
    OUTCOME_TRAFFIC,
    OUTCOME_APP_PROMOTION,

    CONVERSIONS;

    public static MetaCampaignObjective from(String raw) {
        for (MetaCampaignObjective objective : values()) {
            if (objective.name().equalsIgnoreCase(raw)) {
                return objective;
            }
        }
        throw new IllegalArgumentException("Invalid campaign objective: " + raw);
    }
}
