package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class VideoUploadResult extends UploadResult {
    private final String videoId;
}