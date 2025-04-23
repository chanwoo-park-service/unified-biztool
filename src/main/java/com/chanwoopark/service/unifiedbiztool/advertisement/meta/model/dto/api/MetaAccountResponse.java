package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaAccountResponse {
    private String name;
    private String link;
    private Picture picture;
    private String id;
}
