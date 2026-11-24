package com.record.java.controller;

import com.record.java.pojo.CommentPojo;
import com.record.java.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
public class CommentRestController {
    private final PostService postService;
    private final ObjectMapper objectMapper;

    @GetMapping("/getAllPost")
    public List<CommentPojo> getAllPost(){
        //List<Comment> comments = storeInDatabase.saveRecord(postService.fetchAllPost());
        return postService.fetchAllPost();
    }
}
