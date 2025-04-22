package com.chanwoopark.service.unifiedbiztool.advertisement.meta.validation;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.AdRequest;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCreativeFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MetaValidator {

    private final MessageSource messageSource;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/bmp",
            "image/heic",
            "video/mp4",
            "video/quicktime"
    );

    public void validateCreativeFormat(AdRequest request, List<MultipartFile> files, List<MultipartFile> thumbnails) {
        MetaCreativeFormat format = request.getMetaCreativeFormat();
        if (format == null) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(
                            "creative.format.required",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }
        switch (format) {
            case DYNAMIC, COLLECTION -> {
            }
            case SINGLE -> {
                if (files == null || files.size() != 1) {
                    throw new IllegalArgumentException(
                            messageSource.getMessage(
                                    "creative.single.file.required",
                                    null,
                                    LocaleContextHolder.getLocale())
                    );
                }
                validateFileType(files.get(0));


                if (isVideo(files.get(0).getContentType())) {
                    if (thumbnails == null || thumbnails.isEmpty() || !isImage(thumbnails.get(0).getContentType())) {
                        throw new IllegalArgumentException(
                                messageSource.getMessage(
                                        "creative.file.thumbnails.required",
                                        new Object[]{1, thumbnails == null ? 0 : thumbnails.size()},
                                        LocaleContextHolder.getLocale())
                        );
                    }
                }

            }
            case SLIDESHOW -> {

                if (files == null || files.size() < 2) {
                    throw new IllegalArgumentException(
                            messageSource.getMessage(
                                    "creative.slideshow.files.required",
                                    null,
                                    LocaleContextHolder.getLocale())
                    );
                } else if (files.size() > 10) {
                    throw new IllegalArgumentException(
                            messageSource.getMessage(
                                    "creative.slideshow.file.limit.exceeded",
                                    null,
                                    LocaleContextHolder.getLocale())
                    );
                }
                boolean allImages = files.stream().allMatch(f -> isImage(f.getContentType()));

                if (!allImages) {
                    throw new IllegalArgumentException(
                            messageSource.getMessage(
                                    "creative.slideshow.require.images",
                                    null,
                                    LocaleContextHolder.getLocale())
                    );
                }

                files.forEach(this::validateFileType);
            }
        }
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!ALLOWED_MIME_TYPES.contains(contentType))
        ) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(
                            "creative.file.invalid.type",
                            new Object[]{contentType, ALLOWED_MIME_TYPES},
                            LocaleContextHolder.getLocale()
                    )
            );
        }
    }

    private boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    private boolean isVideo(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }

}
