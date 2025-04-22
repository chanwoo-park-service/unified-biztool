package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ImageUploadResult extends UploadResult {
    private final String imageHash;
    private final String url;
}