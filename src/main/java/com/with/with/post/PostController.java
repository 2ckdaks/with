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
        Map<String, Double> location = (Map<String, Double>) session.getAttribute("userLocation");
        Page<Post> posts;

        if (location != null) {
            double lat = location.get("latitude");
            double lon = location.get("longitude");
            posts = postRepository.findByLocationNear(lat, lon, pageable);
        } else {
            // 위치 정보 없이 기본 페이지네이션 사용
            posts = postRepository.findPageBy(pageable);
        }

        model.addAttribute("posts", posts);

        // 로그 출력: 각 게시물의 ID와 현재 페이지 번호
        posts.forEach(post -> System.out.println("Post ID: " + post.getId() + " loaded for page " + number));

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
    String postSearch(@RequestParam String searchText){
        var result = postRepository.rawQuery1(searchText);
        System.out.println("결과" + result);

        return "redirect:/list/page/1";
    }
}
