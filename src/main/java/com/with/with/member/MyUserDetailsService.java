package com.with.with.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        DB에서 username을 가진 유저를 찾아와서
//        return new User(유저아이디, 비번, 권한) 해주세요
        var result = memberRepository.findByUsername(username);
        if (result.isEmpty()) {
            throw new UsernameNotFoundException("계정 없음");
        }
        var user = result.get();

        List<GrantedAuthority> authority = new ArrayList<>();
        authority.add(new SimpleGrantedAuthority("nomal")); //유저유형 메모 "뭐 유형이 관리자면 관리자 if문 사용하면 됨"

//        return new User(user.getUsername(), user.getPassword(), authority);

        var customUser = new CustomUser(user.getUsername(), user.getPassword(), authority);
        customUser.setId(user.getId());  // ID 설정
        customUser.displayName = user.getDisplayName();
        customUser.userType = user.getUserType();
        customUser.profileImageUrl = user.getProfileImageUrl();

        return customUser;
    }
}

