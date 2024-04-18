package com.with.with.post;

import lombok.RequiredArgsConstructor;
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

    @PostMapping("/add-write")
    String addPost(@ModelAttribute PostDto postDto, Authentication authentication){

        postService.createPost(postDto, authentication);

        return "redirect:/";
    }

    @GetMapping("/detail/{id}")
    public String findOne(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.findOne(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            return "detail.html";
        } else {
            return "redirect:/list";
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
    public ResponseEntity<?> deletePost(@RequestParam Long id){
        try {
            postRepository.deleteById(id);
            return ResponseEntity.ok().build(); // 성공적으로 처리되었다는 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패");
        }
    }
}
