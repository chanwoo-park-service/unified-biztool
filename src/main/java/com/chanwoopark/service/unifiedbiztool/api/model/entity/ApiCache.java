package com.chanwoopark.service.unifiedbiztool.api.model.entity;

import com.chanwoopark.service.unifiedbiztool.api.model.enums.ApiStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCache {
    private ApiStatus apiStatus;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    public static ApiCache done() {
        return ApiCache.builder()
                .apiStatus(ApiStatus.DONE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ApiCache pending() {
        return ApiCache.builder()
                .apiStatus(ApiStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
