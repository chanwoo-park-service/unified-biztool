package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Picture {
    private PictureData data;

    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    public static class PictureData {
        private int height;
        private String url;
        private int width;
    }
}
