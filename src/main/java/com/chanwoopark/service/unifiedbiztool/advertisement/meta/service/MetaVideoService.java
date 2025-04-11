package com.chanwoopark.service.unifiedbiztool.advertisement.meta.service;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.UploadResult;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.VideoUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class MetaVideoService {

    public CompletableFuture<UploadResult>  uploadVideo(
            String accountId,
            MultipartFile file
    ) {
        return CompletableFuture.completedFuture(
                VideoUploadResult.builder()
                .build()
        );
    }

}
