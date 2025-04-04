package com.chanwoopark.service.unifiedbiztool.common.model.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record ErrorResponse(
        String timestamp,
        int status,
        String message,
        String path
) {
    public static ErrorResponse of(HttpStatus status, String body, String path) {
        return ErrorResponse.builder()
                .timestamp(now())
                .status(status.value())
                .message(body)
                .path(path)
                .build();
    }

    private static String now() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}