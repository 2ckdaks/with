package com.with.with.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final MemberService memberService;
    private final S3Service s3Service;

    @GetMapping("/sign-up")
    public String signUp(){
        return "signUp.html";
    }

    @PostMapping("/sign-up")
    public String addMember(String username, String password, String displayName, String userType, String profileImageUrl){

        memberService.addMember(username, password, displayName, userType, profileImageUrl);

        return "redirect:/login";
    }

    @GetMapping("/presigned-url")
    @ResponseBody
    String getURL(@RequestParam String filename){
        var result = s3Service.createPreSignedUrl("profile/" + filename);
        System.out.println(result);

        return result;
    }

    @GetMapping("/login")
    public String login(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 이미 인증된 사용자인지 확인 (remember-me 포함)
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // 인증된 사용자가 'anonymousUser'가 아닌 경우
            return "redirect:/my-page";
        }
        return "login.html";
    }

    @GetMapping("/my-page")
    public String myPage(Authentication authentication){
        System.out.println(authentication);
        System.out.println(authentication.getName());
        CustomUser result = ( CustomUser ) authentication.getPrincipal();
        System.out.println(result.displayName);
        System.out.println(result.userType);

        return "mypage.html";
    }

    @GetMapping("/review/{id}")
    public String reviewsByUserId(@PathVariable String id, Model model){
        List<Review> reviews = reviewRepository.findAllByTarget(id);
        model.addAttribute("reviews", reviews);
        return "review.html";  // 모든 리뷰를 보여주는 HTML 페이지
    }

    @PostMapping("/add-review")
    String addReview(@ModelAttribute ReviewDto reviewDto, Authentication authentication){

        memberService.createReview(reviewDto, authentication);

        return "redirect:/review/" + reviewDto.getTarget();
    }
}
