package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.chanwoopark.service.unifiedbiztool.common.model.file.CustomMultipartFile;
import lombok.Builder;
import lombok.Getter;

import java.io.File;

@Builder
@Getter
public class ThumbnailRequest {

    private CustomMultipartFile multipartFile;

    private File absoluteFile;
}
