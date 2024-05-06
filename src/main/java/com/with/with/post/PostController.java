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
    private final ChatController chatController;

    @GetMapping("/write")
    public String write(Model model){
        model.addAttribute("apiKey", kakaoApiKey);
        return "write.html";
    }

    @GetMapping("/list")
    public String findAll(Model model){
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);  // "posts"라는 이름으로 모델에 추가
        return "list.html";  // 해당 뷰에 posts 데이터를 전달
    }

    @PostMapping("/list/location")
    public ResponseEntity<?> receiveLocation(@RequestBody Map<String, Double> location, HttpSession session) {
        session.setAttribute("userLocation", location);
        System.out.println("Received latitude: " + location.get("latitude") + ", longitude: " + location.get("longitude"));
        return ResponseEntity.ok().build();
    }


    @GetMapping("/list/page/{number}")
    public String getListPage(Model model, @PathVariable Integer number, HttpSession session, Pageable pageable) {
        // number에서 1을 빼서 0부터 시작하는 페이지 인덱스로 조정합니다.
        // 모든 페이지 요청에 대해 일관적으로 한 페이지당 5개의 게시물을 보여주도록 설정합니다.
        pageable = PageRequest.of(number - 1, 5);

        Map<String, Double> location = (Map<String, Double>) session.getAttribute("userLocation");
        Page<Post> posts;

        if (location != null) {
            // 위치 정보가 있는 경우, 위치 기반 페이지네이션 사용
            double lat = location.get("latitude");
            double lon = location.get("longitude");
            posts = postService.findPostsByLocation(pageable, lat, lon);
        } else {
            // 위치 정보가 없는 경우, 일반 페이지네이션 사용
            posts = postRepository.findPageBy(pageable);
        }

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

    @PostMapping("/search")
    public String postSearch(@RequestParam String searchText, @RequestParam String searchType, Model model){
        List<Post> result = postService.searchPosts(searchText, searchType);
        System.out.println("결과" + result);
        model.addAttribute("posts", result);
        return "search.html";
    }
}
