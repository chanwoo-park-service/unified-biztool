package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto;

import lombok.Builder;

@Builder
public record VideoChunkResponse(
        String startOffset,
        String endOffset,
        boolean success) {
}
