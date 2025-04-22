package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CampaignObjective {
    SALES("판매"),
    TRAFFIC("트레픽");

    private final String description;


    public static CampaignObjective fromDescription(String description) {
        for (CampaignObjective objective : values()) {
            if (objective.getDescription().equals(description)) {
                return objective;
            }
        }
        throw new IllegalArgumentException("Invalid campaign objective description: " + description);
    }
}
