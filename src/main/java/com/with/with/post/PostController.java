package com.with.with.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;

    @GetMapping("/write")
    public String write(){

        return "write.html";
    }

    @GetMapping("/list")
    public String findAll(Model model){
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);  // "posts"라는 이름으로 모델에 추가
        return "list.html";  // 해당 뷰에 posts 데이터를 전달
    }

    @GetMapping("/list/page/{number}")
    String getListPage(Model model, @PathVariable Integer number){
        Page<Post> posts = postRepository.findPageBy(PageRequest.of(number-1, 5));
        System.out.println(posts.getTotalPages());
        model.addAttribute("posts", posts);

        return "list.html";
    }

    @PostMapping("/add-write")
    String addPost(@ModelAttribute PostDto postDto, Authentication authentication){

        postService.createPost(postDto, authentication);

        return "redirect:/list/page/1";
    }

    @GetMapping("/detail/{id}")
    public String findOne(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.findOne(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            return "detail.html";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/edit/{id}")
    public String update(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.findOne(id);
        if (post.isPresent()){
            model.addAttribute("post", post.get());
            return "edit.html";
        } else {
            return "redirect:/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute PostDto postDto, Authentication authentication) {
        postService.updatePost(postDto, id, authentication);
        return "redirect:/detail/" + id;
    }

    //query string으로 받은경우
    @DeleteMapping("/post")
    public ResponseEntity<String> deletePost(@RequestParam Long id, Authentication authentication) {
        return postService.deletePost(id, authentication);
    }
}
