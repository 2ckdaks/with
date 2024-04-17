package com.with.with.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
//        System.out.println(memberRepository.findByUsername("username"));
        return "login.html";
    }
}
