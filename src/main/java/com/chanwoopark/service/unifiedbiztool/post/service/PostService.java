package com.chanwoopark.service.unifiedbiztool.post.service;

import com.chanwoopark.service.unifiedbiztool.post.model.dto.PostRequest;
import com.chanwoopark.service.unifiedbiztool.post.model.dto.PostResponse;
import com.chanwoopark.service.unifiedbiztool.post.model.entity.Post;
import com.chanwoopark.service.unifiedbiztool.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public PostResponse createPost(PostRequest postRequest) {
        Post savedPost = postRepository.save(postRequest);
        return PostResponse.from(savedPost);
    }

    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        return PostResponse.from(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        postRepository.deleteById(id);
    }

    public void updatePost(PostRequest postRequest) {
        postRepository.update(postRequest);
    }



}
