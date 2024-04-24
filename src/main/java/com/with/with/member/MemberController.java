package com.with.with.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @GetMapping("/sign-up")
    public String signUp(){
        return "signUp.html";
    }

    @PostMapping("/sign-up")
    public String addMember(String username, String password, String displayName, String userType){

        memberService.addMember(username, password, displayName, userType);

        return "redirect:/login";
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
}
