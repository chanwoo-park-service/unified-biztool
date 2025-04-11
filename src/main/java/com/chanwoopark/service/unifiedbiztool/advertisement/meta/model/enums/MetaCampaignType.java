package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum MetaCampaignType {
    ABO("기본 메타"),
    ASC("어드밴티지+쇼핑 캠페인"),
    DEFAULT("");

    private final String description;

    @JsonCreator
    public static MetaCampaignType from(String value) {
        if (value == null || value.isBlank()) return DEFAULT;

        return Arrays.stream(values())
                .filter(type -> type.getDescription().equals(value.trim()))
                .findFirst()
                .orElse(DEFAULT);
    }
}
