package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Picture {
    private PictureData data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    public static class PictureData {
        private int height;
        private String url;
        private int width;
    }
}
