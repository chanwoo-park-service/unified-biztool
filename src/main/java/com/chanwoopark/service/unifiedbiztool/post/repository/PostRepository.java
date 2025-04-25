package com.chanwoopark.service.unifiedbiztool.post.repository;

import com.chanwoopark.service.unifiedbiztool.post.model.dto.PostRequest;
import com.chanwoopark.service.unifiedbiztool.post.model.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Repository
public class PostRepository {

    private static final String POST_KEY_PREFIX = "post:";

    private final RedisTemplate<String, Post> postRedisTemplate;

    public Post save(PostRequest postRequest) {
        String uuid = UUID.randomUUID().toString();
        Post post = Post.builder()
                .id(Long.parseLong(uuid.hashCode() + ""))
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .createdAt(LocalDateTime.now())
                .createdBy(0L)
                .build();

        String key = POST_KEY_PREFIX + post.getId();
        postRedisTemplate.opsForValue().set(key, post);

        return post;
    }

    public Post update(PostRequest postRequest) {
        Post post = Post.builder()
                .id(postRequest.getId())
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .createdBy(0L)
                .build();

        String key = POST_KEY_PREFIX + post.getId();
        postRedisTemplate.opsForValue().set(key, post);

        return post;
    }


    public Optional<Post> findById(Long id) {
        String key = POST_KEY_PREFIX + id;
        Post post = postRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(post);
    }

    public List<Post> findAll() {
        List<Post> posts = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match("post:*").build();

        Cursor<String> cursor = postRedisTemplate.scan(options);
        while (cursor.hasNext()) {
            String key = cursor.next();
            Post post = postRedisTemplate.opsForValue().get(key);

            if (post != null) {
                posts.add(post);
            }
        }
        return posts;
    }

    public void deleteById(Long id) {
        String key = POST_KEY_PREFIX + id;
        postRedisTemplate.delete(key);
    }

}
