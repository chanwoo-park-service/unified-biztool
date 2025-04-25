package com.chanwoopark.service.unifiedbiztool.post.model.dto;

import com.chanwoopark.service.unifiedbiztool.post.model.entity.Post;
import lombok.*;

import java.time.LocalDateTime;


@ToString
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long createdBy;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .createdBy(post.getCreatedBy())
                .build();
    }
}
