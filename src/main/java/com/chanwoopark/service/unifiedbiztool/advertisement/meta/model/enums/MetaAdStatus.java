package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;

@Getter
public enum MetaAdStatus {
    ACTIVE,
    PAUSED,
    DELETED,
    ARCHIVED;

    public static MetaAdStatus from(String raw) {
        for (MetaAdStatus status : values()) {
            if (status.name().equalsIgnoreCase(raw)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ad status: " + raw);
    }
}