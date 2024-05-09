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

    private final MemberService memberService;
    private final S3Service s3Service;

    // 회원가입 페이지 반환
    @GetMapping("/sign-up")
    public String signUp(){
        return "signUp.html";
    }

    // 아이디 중복 확인
    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<String> checkUsername(@RequestParam String username) {
        boolean isAvailable = memberService.checkUsernameAvailability(username);
        System.out.println("Username checked: " + username + " - Available: " + isAvailable);
        return isAvailable ?
                ResponseEntity.ok("Available") :
                ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
    }

    // 닉네임 중복 확인
    @GetMapping("/check-displayname")
    @ResponseBody
    public ResponseEntity<String> checkDisplayname(@RequestParam String displayName) {
        boolean isAvailable = memberService.checkDisplaynameAvailability(displayName);
        System.out.println("Display name checked: " + displayName + " - Available: " + isAvailable);
        return isAvailable ?
                ResponseEntity.ok("Available") :
                ResponseEntity.status(HttpStatus.CONFLICT).body("Display name already taken");
    }

    // 회원가입 처리
    @PostMapping("/sign-up")
    public String addMember(String username, String password, String displayName, String userType, String profileImageUrl){
        if (!memberService.checkUsernameAvailability(username) || password.length() < 8) {
            // 사용자에게 이미 사용 중인 아이디임을 알리고 회원가입 페이지로 리다이렉트
            return "redirect:/sign-up?error=usernameTaken";
        }
        memberService.addMember(username, password, displayName, userType, profileImageUrl);
        return "redirect:/login";
    }

    // 프로필 이미지 업로드를 위한 사전 서명된 URL 반환
    @GetMapping("/presigned-url")
    @ResponseBody
    String getURL(@RequestParam String filename){
        return memberService.getPreSignedUrl(filename);
    }

    // 로그인 페이지 반환
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

    // 마이페이지 반환
    @GetMapping("/my-page")
    public String myPage(Authentication authentication){
//        System.out.println(authentication);
//        System.out.println(authentication.getName());
        CustomUser result = ( CustomUser ) authentication.getPrincipal();
//        System.out.println(result.displayName);
//        System.out.println(result.userType);
        return "mypage.html";
    }

    // 마이페이지 수정
    @PostMapping("/my-page/{id}")
    public String update(@PathVariable Long id, Authentication authentication, String displayName) {
        memberService.updateMember(id, authentication, displayName);
        return "redirect:/my-page";
    }

    // 방명록 조회
    @GetMapping("/review/{id}")
    public String reviewsByUserId(@PathVariable String id, Model model) {
        List<Review> reviews = memberService.findReviewsByTarget(id);
        model.addAttribute("targetId", id);  // 리뷰 대상자의 ID를 모델에 추가
        model.addAttribute("reviews", reviews);  // 조회된 리뷰 목록을 모델에 추가
//        System.out.println(reviews);
        return "review.html";  // 리뷰 페이지를 반환
    }


    // 방명록 작성
    @PostMapping("/add-review")
    String addReview(@ModelAttribute ReviewDto reviewDto, Authentication authentication){
        memberService.createReview(reviewDto, authentication);
        return "redirect:/review/" + reviewDto.getTarget();
    }
}