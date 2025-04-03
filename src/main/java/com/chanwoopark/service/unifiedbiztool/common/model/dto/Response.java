package com.chanwoopark.service.unifiedbiztool.common.model.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record Response<T>(
        String timestamp,
        int status,
        T data
) {
    public static <T> Response<T> of(HttpStatus status, T data) {
        return Response.<T>builder()
                .timestamp(now())
                .status(status.value())
                .data(data)
                .build();
    }

    private static String now() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}