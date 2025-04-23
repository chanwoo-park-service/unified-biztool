package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web;


import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.UploadResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AdResponse {

    private Long index;
    private List<UploadResult> uploadResults;
}
