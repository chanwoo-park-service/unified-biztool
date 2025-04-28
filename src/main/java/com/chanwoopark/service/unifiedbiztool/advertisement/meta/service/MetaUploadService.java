package com.chanwoopark.service.unifiedbiztool.advertisement.meta.service;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.*;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaVideoUploadPhase;
import com.chanwoopark.service.unifiedbiztool.common.http.HttpClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Service
public class MetaUploadService {

    private final HttpClientHelper httpClientHelper;

    private final MessageSource messageSource;

    private final String META_URL = "https://graph.facebook.com";

    private final ObjectMapper objectMapper;

    private final ThumbnailService thumbnailService;

    @Async
    public CompletableFuture<UploadResult> uploadVideo(
            String accountId,
            MultipartFile file,
            String accessToken
    ) {
        long fileSize = file.getSize();
        String originalFilename = file.getOriginalFilename();
        String uploadSessionId = null;

        String url = META_URL
                + "/v22.0/"
                + Objects.requireNonNull(accountId)
                + "/advideos";

        String response = httpClientHelper.postFormIgnoreFail(
                url,
                form -> form
                        .with("access_token", accessToken)
                        .with("upload_phase", MetaVideoUploadPhase.START.getPhase())
                        .with("file_size", String.valueOf(fileSize))
        );

        try {
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                JsonNode error = root.get("error");
                String title = error.path("error_user_title").asText(null);
                String msg = error.path("error_user_msg").asText(null);
                String fallback = error.path("message").asText(
                        messageSource.getMessage(
                                "meta.api.error.generic",
                                null,
                                LocaleContextHolder.getLocale()
                        )
                );

                String reason = (title != null && msg != null) ? title + ": " + msg : fallback;
                return CompletableFuture.completedFuture(
                        VideoUploadResult.builder()
                                .success(false)
                                .reason(reason)
                                .build()
                );
            }

            String videoId = root.path("video_id").asText(null);
            uploadSessionId = root.path("upload_session_id").asText(null);
            String startOffset = root.path("start_offset").asText(null);
            String endOffset = root.path("end_offset").asText(null);


            while (!startOffset.equals(endOffset)) {
                VideoChunkResponse chunk = transferChunk(startOffset, endOffset, url, uploadSessionId, file, accessToken);
                if (!chunk.success()) {
                    postCancel(url, uploadSessionId, accessToken);
                    return CompletableFuture.completedFuture(
                            VideoUploadResult.builder()
                                    .success(false)
                                    .reason(
                                            messageSource.getMessage(
                                                    "meta.api.error.video.chunk",
                                                    null,
                                                    LocaleContextHolder.getLocale()
                                            )
                                    )
                                    .build()
                    );
                }
                startOffset = chunk.startOffset();
                endOffset = chunk.endOffset();
            }
            String finalUploadSessionId = uploadSessionId;
            String finishResponse = httpClientHelper.postFormIgnoreFail(url,
                    form -> form
                            .with("access_token", accessToken)
                            .with("upload_phase", MetaVideoUploadPhase.FINISH.getPhase())
                            .with("upload_session_id", finalUploadSessionId)
            );

            boolean isFinishSuccess = false;
            try {
                JsonNode finishRoot = objectMapper.readTree(finishResponse);
                isFinishSuccess = finishRoot.path("success").asBoolean(false);
            } catch (Exception e) {
                log.warn("[Meta Finish Upload] 응답 파싱 실패: {}", finishResponse);
            }
            ThumbnailRequest thumbnailRequest = thumbnailService.extractThumbnail(file);

            return CompletableFuture.completedFuture(
                    VideoUploadResult.builder()
                            .success(isFinishSuccess)
                            .videoId(videoId)
                            .originalFilename(originalFilename != null ? originalFilename : "")
                            .thumbnailResult(uploadThumbnail(accountId, thumbnailRequest, accessToken).join())
                            .reason(isFinishSuccess ?
                                    null :
                                    messageSource.getMessage(
                                            "meta.api.error.video.chunk",
                                            null,
                                            LocaleContextHolder.getLocale()
                                    )
                            )
                            .build()
            );

        } catch (IOException e) {
            postCancel(url, uploadSessionId, accessToken);

            String reason =
                    messageSource.getMessage(
                            "meta.api.error.unknown",
                            null,
                            LocaleContextHolder.getLocale()
                    )
                            + ": "
                            + e.getMessage();

            return CompletableFuture.completedFuture(
                    VideoUploadResult.builder()
                            .success(false)
                            .reason(reason)
                            .build()
            );
        }
    }

    private VideoChunkResponse transferChunk(
            String startOffset,
            String endOffset,
            String url,
            String uploadSessionId,
            MultipartFile multipartFile,
            String accessToken
    ) {

        try {
            ByteArrayResource chunkResource = getChunkResource(startOffset, endOffset, multipartFile);

            String response = httpClientHelper.postMultipartIgnoreFail(
                    url,
                    form -> {
                        form.part("access_token", accessToken);
                        form.part("video_file_chunk", chunkResource);
                        form.part("upload_session_id", uploadSessionId);
                        form.part("upload_phase", MetaVideoUploadPhase.TRANSFER.getPhase());
                        form.part("start_offset", startOffset);
                        form.part("end_offset", endOffset);
                    }
            );
            log.info("[Meta Chunk Upload] 응답: {}", response);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                log.error("[Meta Chunk Upload] 오류 응답: {}", root.get("error"));
                return VideoChunkResponse.builder()
                        .startOffset(startOffset)
                        .endOffset(endOffset)
                        .success(false)
                        .build();
            }

            String newStartOffset = root.path("start_offset").asText(null);
            String newEndOffset = root.path("end_offset").asText(null);
            return VideoChunkResponse.builder()
                    .startOffset(newStartOffset)
                    .endOffset(newEndOffset)
                    .success(true)
                    .build();
        } catch (IOException ioException) {
            postCancel(url, uploadSessionId, accessToken);
            return VideoChunkResponse.builder()
                    .success(false)
                    .build();
        }
    }

    private void postCancel(String url, String uploadSessionId, String accessToken) {
        log.warn("[청크 API 업로드 실패] : {}", uploadSessionId);
        httpClientHelper.postFormIgnoreFail(url,
                form -> form
                        .with("access_token", accessToken)
                        .with("upload_phase", MetaVideoUploadPhase.CANCEL.getPhase())
                        .with("upload_session_id", uploadSessionId)
        );
    }

    private ByteArrayResource getChunkResource(String startOffset, String endOffset, MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension;

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        } else {
            extension = ".dat";
        }

        InputStream inputStream = multipartFile.getInputStream();
        long start = Long.parseLong(startOffset);
        long end = Long.parseLong(endOffset);
        int chunkSize = (int)(end - start);

        long skipped = 0;
        while (skipped < start) {
            long n = inputStream.skip(start - skipped);
            if (n <= 0) {
                throw new IOException(
                        messageSource.getMessage(
                                "file.stream.skip.failure",
                                null,
                                LocaleContextHolder.getLocale()
                        )
                );
            }
            skipped += n;
        }
        byte[] buffer = inputStream.readNBytes(chunkSize);

        final long finalStart = start;
        final long finalEnd = end;
        final String finalExtension = extension;

        return new ByteArrayResource(buffer) {
            @Override
            public String getFilename() {
                String uniqueId = UUID.randomUUID().toString();
                return "chunk_" + finalStart + "_" + finalEnd + "_" + uniqueId + finalExtension;
            }
        };
    }

    private CompletableFuture<UploadResult> uploadThumbnail(String adAccountId, ThumbnailRequest thumbnailRequest, String accessToken) {
        return uploadImage(
                adAccountId,
                thumbnailRequest.getMultipartFile(),
                accessToken
        ).whenComplete((result, ex) -> {
            if (ex == null && result.isSuccess()) {
                boolean deleted = thumbnailRequest.getAbsoluteFile().delete();
                if (!deleted) {
                    log.warn("Thumbnail file deletion failed: {}", thumbnailRequest.getAbsoluteFile().getAbsolutePath());
                }
            } else {
                log.error("Thumbnail upload failed or was interrupted: {}", ex != null ? ex.getMessage() : "Unknown error");
            }
        });
    }

    @Async
    public CompletableFuture<UploadResult> uploadImage(String adAccountId, MultipartFile file, String accessToken) {
        String originalFilename = file.getOriginalFilename();
        try {
            byte[] fileBytes = file.getBytes();
            String encoded = Base64.getEncoder().encodeToString(fileBytes);
            String response = httpClientHelper.postFormIgnoreFail(META_URL
                            + "/v22.0/"
                            + adAccountId
                            + "/adimages",
                    form -> form
                            .with("access_token", accessToken)
                            .with("bytes", encoded)
            );
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                return CompletableFuture.completedFuture(
                        buildErrorResult(originalFilename, root.get("error"))
                );
            }
            String hash = root.path("images").path("bytes").path("hash").asText();
            String url = root.path("images").path("bytes").path("url").asText();
            boolean isSuccess = hash != null && !hash.isBlank();

            return CompletableFuture.completedFuture(
                    ImageUploadResult.builder()
                            .originalFilename(originalFilename)
                            .imageHash(hash)
                            .url(url)
                            .success(isSuccess)
                            .reason(
                                    isSuccess ? null :
                                            messageSource.getMessage(
                                                    "creative.file.hash.missing",
                                                    null,
                                                    LocaleContextHolder.getLocale()
                                            )
                            )
                            .build()
            );
        } catch (Exception ex) {
            String defaultMessage = messageSource.getMessage(
                    "validation.default",
                    null,
                    LocaleContextHolder.getLocale()
            );

            return CompletableFuture.completedFuture(
                    ImageUploadResult.builder()
                            .originalFilename(originalFilename)
                            .imageHash(null)
                            .success(false)
                            .reason(defaultMessage + ": " + ex.getMessage())
                            .build()
            );
        }
    }

    private ImageUploadResult buildErrorResult(String originalFilename, JsonNode errorNode) {
        String title = errorNode.path("error_user_title").asText(null);
        String msg = errorNode.path("error_user_msg").asText(null);
        String fallback = errorNode.path("message").asText(
                messageSource.getMessage(
                        "validation.default",
                        null,
                        LocaleContextHolder.getLocale()
                )
        );
        return ImageUploadResult.builder()
                .originalFilename(originalFilename)
                .imageHash(null)
                .success(false)
                .reason(title != null ? title + ": " + msg : fallback)
                .build();
    }

}
