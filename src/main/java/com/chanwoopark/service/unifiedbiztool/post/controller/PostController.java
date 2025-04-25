package com.chanwoopark.service.unifiedbiztool.post.controller;

import com.chanwoopark.service.unifiedbiztool.post.model.dto.PostRequest;
import com.chanwoopark.service.unifiedbiztool.post.model.dto.PostResponse;
import com.chanwoopark.service.unifiedbiztool.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/posts")
@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;


    @GetMapping("")
    public ModelAndView getPosts(@RequestParam(value = "postId", required = true) Long postId) {
        ModelAndView modelAndView = new ModelAndView("post");
        PostResponse post = postService.getPost(postId);
        modelAndView.addObject("post", post);

        return modelAndView;
    }

    @GetMapping("/write")
    public ModelAndView writePostForm() {
        return new ModelAndView("post-write");
    }

    @PostMapping("/save")
    public ModelAndView savePost(PostRequest postRequest) {
        PostResponse savedPost = postService.createPost(postRequest);
        return new ModelAndView("redirect:/posts?postId=" + savedPost.getId());
    }

    @GetMapping("/edit")
    public ModelAndView editPostForm(@RequestParam(value = "postId", required = true) Long postId) {
        PostResponse post = postService.getPost(postId);
        ModelAndView modelAndView = new ModelAndView("post-edit");
        modelAndView.addObject("post", post);
        return modelAndView;
    }

    @PostMapping ("/update")
    public ModelAndView updatePost(PostRequest postRequest) {
        postService.updatePost(postRequest);
        return new ModelAndView("redirect:/posts?postId=" + postRequest.getId());
    }

    @PostMapping("/delete")
    public ModelAndView deletePost(PostRequest postRequest) {
        postService.deletePost(postRequest.getId());
        return new ModelAndView("redirect:/");
    }
}
