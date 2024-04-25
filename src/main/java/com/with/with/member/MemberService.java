package com.with.with.member;

import com.with.with.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public void addMember(String username, String password, String displayName, String userType, String profileImageUrl){
        Member member = new Member();

//        String hashPassword = new BCryptPasswordEncoder().encode(password);
        String hashPassword = passwordEncoder.encode(password); //다른 class를 디펜던시 인젝션으로 사용
        System.out.println(hashPassword);

        member.setUsername(username);
        member.setPassword(hashPassword); //해싱해줘야함
        member.setDisplayName(displayName);
        member.setUserType(userType);
        member.setProfileImageUrl(profileImageUrl);

        memberRepository.save(member);
    }

    public Optional<Review> findReivew(Long id) {
        return reviewRepository.findById(id);
    }

    @Transactional
    public Review createReview(ReviewDto reviewDto, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();  // 현재 로그인한 사용자 정보

        Review newReview = new Review();
        newReview.setReview(reviewDto.getReview());
        newReview.setWriter(user.getUsername());  // 현재 사용자의 username을 작성자로 설정
        newReview.setTarget(reviewDto.getTarget());  // DTO에서 받은 target ID 설정

        Review savedReview = reviewRepository.save(newReview);
        return savedReview;
    }
}
