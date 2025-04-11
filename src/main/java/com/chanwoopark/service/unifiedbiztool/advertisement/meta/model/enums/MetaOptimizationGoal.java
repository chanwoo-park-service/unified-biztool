package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;

@Getter
public enum MetaOptimizationGoal {
    NONE,
    APP_INSTALLS,
    AD_RECALL_LIFT,
    ENGAGED_USERS,
    EVENT_RESPONSES,
    IMPRESSIONS,
    LEAD_GENERATION,
    QUALITY_LEAD,
    LINK_CLICKS,
    OFFSITE_CONVERSIONS,
    PAGE_LIKES,
    POST_ENGAGEMENT,
    QUALITY_CALL,
    REACH,
    LANDING_PAGE_VIEWS,
    VISIT_INSTAGRAM_PROFILE,
    VALUE,
    THRUPLAY,
    DERIVED_EVENTS,
    APP_INSTALLS_AND_OFFSITE_CONVERSIONS,
    CONVERSATIONS,
    IN_APP_VALUE,
    MESSAGING_PURCHASE_CONVERSION,
    SUBSCRIBERS,
    REMINDERS_SET,
    MEANINGFUL_CALL_ATTEMPT,
    PROFILE_VISIT,
    PROFILE_AND_PAGE_ENGAGEMENT,
    ADVERTISER_SILOED_VALUE,
    MESSAGING_APPOINTMENT_CONVERSION;

    public static MetaOptimizationGoal from(String raw) {
        for (MetaOptimizationGoal goal : values()) {
            if (goal.name().equalsIgnoreCase(raw)) {
                return goal;
            }
        }
        throw new IllegalArgumentException("Invalid optimization_goal: " + raw);
    }
}
