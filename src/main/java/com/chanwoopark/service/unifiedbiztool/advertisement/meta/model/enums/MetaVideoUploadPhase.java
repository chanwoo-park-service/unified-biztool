package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MetaVideoUploadPhase {
    START("start"),
    TRANSFER("transfer"),
    FINISH("finish"),
    CANCEL("cancel");

    private final String phase;

    @Override
    public String toString() {
        return phase;
    }
}