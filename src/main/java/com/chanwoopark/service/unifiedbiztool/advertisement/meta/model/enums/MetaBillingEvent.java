package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;

@Getter
public enum MetaBillingEvent {
    APP_INSTALLS,
    CLICKS,
    IMPRESSIONS,
    LINK_CLICKS,
    NONE,
    OFFER_CLAIMS,
    PAGE_LIKES,
    POST_ENGAGEMENT,
    THRUPLAY,
    PURCHASE,
    LISTING_INTERACTION;

    public static MetaBillingEvent from(String raw) {
        for (MetaBillingEvent event : values()) {
            if (event.name().equalsIgnoreCase(raw)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Invalid billing_event: " + raw);
    }
}