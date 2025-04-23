package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MetaCreativeFormat {
    DYNAMIC("다이내믹"),
    SINGLE("단일 이미지·동영상"),
    SLIDESHOW("슬라이드"),
    COLLECTION("컬렉션");

    private final String description;

    MetaCreativeFormat(String description) {
        this.description = description;
    }

    public static MetaCreativeFormat fromDescription(String description) {
        return Arrays.stream(values())
                .filter(format -> format.getDescription().equals(description))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid creative format description: " + description));
    }

}