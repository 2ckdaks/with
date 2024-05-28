package com.with.with.post;

import com.with.with.chat.ChatController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PostController {
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final PostService postService;
    private final PostRepository postRepository;

    // 게시물 작성 페이지 반환
    @GetMapping("/write")
    public String write(Model model){
        model.addAttribute("apiKey", kakaoApiKey);
        return "write.html";
    }

    // 모든 게시물 조회
    @GetMapping("/list")
    public String findAll(Model model){
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);  // "posts"라는 이름으로 모델에 추가
        return "redirect:/list/page/1";  // 첫 번째 페이지로 리다이렉트
    }

    // 사용자 위치 정보 받기
    @PostMapping("/list/location")
    public ResponseEntity<List<Post>> receiveLocation(@RequestBody Map<String, Double> location, HttpSession session) {
        session.setAttribute("userLocation", location);
        Pageable pageable = PageRequest.of(0, 5); // 첫 페이지를 반환하도록 설정
        double lat = location.get("latitude");
        double lon = location.get("longitude");
        Page<Post> posts = postService.findPostsByLocation(pageable, lat, lon);
        return ResponseEntity.ok(posts.getContent());
    }

    // 페이징된 게시물 조회
    @GetMapping("/list/page/{number}")
    public String getListPage(Model model, @PathVariable Integer number, HttpSession session) {
        Map<String, Double> location = (Map<String, Double>) session.getAttribute("userLocation");
        Page<Post> posts = postService.getPostsByPageAndLocation(number, location, session);

        model.addAttribute("posts", posts);
        return "list.html";
    }

    // 게시물 작성
    @PostMapping("/add-write")
    String addPost(@ModelAttribute PostDto postDto, Authentication authentication){

        postService.createPost(postDto, authentication);

        return "redirect:/list/page/1";
    }

    // 게시물 상세 조회
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

    // 게시물 수정 페이지 반환
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

    // 게시물 수정
    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute PostDto postDto, Authentication authentication) {
        postService.updatePost(postDto, id, authentication);
        return "redirect:/detail/" + id;
    }

    // 게시물 삭제
    //query string으로 받은경우
    @DeleteMapping("/post")
    public ResponseEntity<String> deletePost(@RequestParam Long id, Authentication authentication) {
        return postService.deletePost(id, authentication);
    }

    // 게시물 검색
    @PostMapping("/search")
    public String postSearch(@RequestParam String searchText, @RequestParam String searchType, Model model){
        List<Post> result = postService.searchPosts(searchText, searchType);
//        System.out.println("결과" + result);
        model.addAttribute("posts", result);
        return "search.html";
    }
}