package com.with.with.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void addMember(String username, String password, String displayName, String userType){
        Member member = new Member();

//        String hashPassword = new BCryptPasswordEncoder().encode(password);
        String hashPassword = passwordEncoder.encode(password); //다른 class를 디펜던시 인젝션으로 사용
        System.out.println(hashPassword);

        member.setUsername(username);
        member.setPassword(hashPassword); //해싱해줘야함
        member.setDisplayName(displayName);
        member.setUserType(userType);

        memberRepository.save(member);
    }
}
