package com.with.with.post;

import com.with.with.member.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> findAll(){
        return postRepository.findAll();  // List<Post> 반환
    }

    public Optional<Post> findOne(Long id) {
        return postRepository.findById(id);
    }

    public Page<Post> findPostsByLocation(Pageable pageable, double latitude, double longitude) {
        return postRepository.findByLocationNear(latitude, longitude, pageable);
    }

    public Page<Post> findPageBy(Pageable pageable) {
        return postRepository.findPageBy(pageable);
    }

    public List<Post> searchPosts(String searchText, String searchType) {
        return postRepository.searchByStartOrEndPoint(searchText);
    }

    @Transactional
    public Post createPost(PostDto postDto, Authentication authentication){
        CustomUser user = (CustomUser) authentication.getPrincipal();

        Post newPost = new Post();
        newPost.setStartPoint(postDto.getStartPoint());
        newPost.setStartLatitude(postDto.getStartLatitude()); // 출발지 위도
        newPost.setStartLongitude(postDto.getStartLongitude()); // 출발지 경도
        newPost.setEndPoint(postDto.getEndPoint());
        newPost.setEndLatitude(postDto.getEndLatitude()); // 도착지 위도
        newPost.setEndLongitude(postDto.getEndLongitude()); // 도착지 경도
        newPost.setDate(postDto.getDate());
        newPost.setTime(postDto.getTime());
        newPost.setPersonnel(postDto.getPersonnel());
        newPost.setWriter(user.getUsername());

        Post savedPost = postRepository.save(newPost);
        return savedPost;
    }


    public Post updatePost(PostDto postDto, Long id, Authentication authentication) {
        Optional<Post> existingPost = postRepository.findById(id);
        if (!existingPost.isPresent()) {
            throw new IllegalArgumentException("게시물을 찾을 수 없습니다.");
        }

        Post post = existingPost.get();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        // 작성자와 로그인한 사용자가 다르면 수정 권한이 없음을 알림
        if (!post.getWriter().equals(user.getUsername())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        // 입력된 데이터로 게시물 업데이트
        post.setStartPoint(postDto.getStartPoint());
        post.setStartLatitude(postDto.getStartLatitude()); // 출발지 위도
        post.setStartLongitude(postDto.getStartLongitude()); // 출발지 경도
        post.setEndPoint(postDto.getEndPoint());
        post.setEndLatitude(postDto.getEndLatitude()); // 도착지 위도
        post.setEndLongitude(postDto.getEndLongitude()); // 도착지 경도
        post.setDate(postDto.getDate());
        post.setTime(postDto.getTime());
        post.setPersonnel(postDto.getPersonnel());
        post.setWriter(user.getUsername()); // 현재 로그인한 사용자의 username으로 설정

        return postRepository.save(post);
    }


    public ResponseEntity<String> deletePost(Long id, Authentication authentication) {
        Optional<Post> post = postRepository.findById(id);
        if (!post.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시물을 찾을 수 없습니다.");
        }

        String loggedInUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
        String postOwner = post.get().getWriter();

        if (!loggedInUsername.equals(postOwner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        }

        try {
            postRepository.deleteById(id);
            return ResponseEntity.ok("게시물이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 오류가 발생했습니다.");
        }
    }
}
