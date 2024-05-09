package com.with.with.member;

import com.with.with.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    // 사용자 아이디 중복 확인
    public boolean checkUsernameAvailability(String username) {
        Optional<Member> member = memberRepository.findByUsername(username);
        boolean isPresent = member.isPresent();
        System.out.println("Checking database for username: " + username + " - Found: " + isPresent);
        return !isPresent;
    }

    // 사용자 닉네임 중복 확인
    public boolean checkDisplaynameAvailability(String displayName) {
        Optional<Member> member = memberRepository.findByDisplayName(displayName);
        boolean isPresent = member.isPresent();
        System.out.println("Checking database for displayName: " + displayName + " - Found: " + isPresent);
        return !isPresent;
    }

    // 회원 추가
    public void addMember(String username, String password, String displayName, String userType, String profileImageUrl){
        Member member = new Member();

        String hashPassword = passwordEncoder.encode(password); //다른 class를 디펜던시 인젝션으로 사용

        member.setUsername(username);
        member.setPassword(hashPassword); //보안을위한 비밀번호 암호화
        member.setDisplayName(displayName);
        member.setUserType(userType);
        member.setProfileImageUrl(profileImageUrl);

        memberRepository.save(member);
    }

    //회원 수정
    public void updateMember(Long id, Authentication authentication, String displayName) {
        CustomUser customUser = (CustomUser) authentication.getPrincipal();
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원 ID입니다."));

        // 현재 로그인한 사용자와 회원 정보의 사용자가 일치하는지 확인
        if (!customUser.getUsername().equals(member.getUsername())) {
            throw new IllegalArgumentException("자신의 프로필만 수정할 수 있습니다.");
        }

        // displayName 중복 검사
        Optional<Member> existingMember = memberRepository.findByDisplayName(displayName);
        if (existingMember.isPresent() && !existingMember.get().getId().equals(member.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // displayName 업데이트
        member.setDisplayName(displayName);
        memberRepository.save(member);

        // Security Context 내 Authentication 객체 업데이트
        customUser.displayName = displayName;
        Authentication newAuth = new UsernamePasswordAuthenticationToken(customUser, authentication.getCredentials(), authentication.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    // 대상 사용자의 모든 리뷰 조회
    public List<Review> findReviewsByTarget(String targetId) {
        return reviewRepository.findAllByTarget(targetId);
    }

    // S3 버킷에 사전 서명된 URL 생성
    public String getPreSignedUrl(String filename) {
        return s3Service.createPreSignedUrl("profile/" + filename);
    }

    // 방명록 작성
    @Transactional
    public Review createReview(ReviewDto reviewDto, Authentication authentication) {
        try {
            CustomUser user = (CustomUser) authentication.getPrincipal();  // 현재 로그인한 사용자 정보

            if (user == null) {
                throw new ReviewCreationException("인증된 사용자를 찾을 수 없습니다.");
            }

            Review newReview = new Review();
            newReview.setReview(reviewDto.getReview());
            newReview.setWriter(user.getUsername());  // 현재 사용자의 username을 작성자로 설정
            newReview.setTarget(reviewDto.getTarget());  // DTO에서 받은 target ID 설정

            return reviewRepository.save(newReview);
        } catch (DataAccessException e) {
            throw new ReviewCreationException("리뷰 생성 중 데이터베이스 접근 오류가 발생했습니다.", e);
        } catch (ReviewCreationException e) {
            // 이미 사용자 정의 예외이므로 그대로 다시 던지기
            throw e;
        } catch (Exception e) {
            // 예상치 못한 다른 예외들 처리
            throw new ReviewCreationException("리뷰 생성 중 예상치 못한 오류가 발생했습니다.", e);
        }
    }
}
