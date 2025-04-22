package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
public abstract class UploadResult {
    private final String originalFilename;
    private final boolean success;
    private final String reason;
}
