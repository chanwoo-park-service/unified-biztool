package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

public enum MetaSpecialAdCategory {
    NONE,
    EMPLOYMENT,
    HOUSING,
    CREDIT,
    ISSUES_ELECTIONS_POLITICS,
    ONLINE_GAMBLING_AND_GAMING,
    FINANCIAL_PRODUCTS_SERVICES;

    public static MetaSpecialAdCategory from(String raw) {
        for (MetaSpecialAdCategory category : values()) {
            if (category.name().equalsIgnoreCase(raw)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid special_ad_category: " + raw);
    }
}