package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaIdResponse {
    private List<MetaId> data;
}
