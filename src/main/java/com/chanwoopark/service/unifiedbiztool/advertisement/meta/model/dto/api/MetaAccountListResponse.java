package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaAccountListResponse {
    private List<MetaAccountResponse> data;
}