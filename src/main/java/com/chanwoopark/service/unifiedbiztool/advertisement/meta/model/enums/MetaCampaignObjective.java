package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MetaCampaignObjective {
    OUTCOME_SALES("판매"),
    OUTCOME_TRAFFIC("트래픽");

    private final String description;

    public static MetaCampaignObjective from(String raw) {
        for (MetaCampaignObjective objective : values()) {
            if (objective.getDescription().equalsIgnoreCase(raw.trim())) {
                return objective;
            }
        }
        throw new IllegalArgumentException("Invalid campaign objective: " + raw);
    }
}
