package com.with.with.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/write")
    public String write(){

        return "write.html";
    }

    @PostMapping("/add-write")
    String addPost(@ModelAttribute PostDto postDto){

        postService.createPost(postDto);

        return "redirect:/";
    }
}
