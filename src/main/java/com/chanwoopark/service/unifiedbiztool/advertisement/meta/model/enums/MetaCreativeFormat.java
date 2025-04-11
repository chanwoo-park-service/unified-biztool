package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;

@Getter
public enum MetaCreativeFormat {
    DYNAMIC("다이내믹"),
    SINGLE("단일 이미지 또는 동영상"),
    SLIDESHOW("플라이드"),
    COLLECTION("컬렉션");

    private final String description;

    MetaCreativeFormat(String description) {
        this.description = description;
    }

}