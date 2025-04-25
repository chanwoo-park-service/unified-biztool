package com.chanwoopark.service.unifiedbiztool.post.model.dto;

import lombok.*;


@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Setter
@Getter
public class PostRequest {
    private String title;
    private String content;
    @Setter
    private Long id;
}
