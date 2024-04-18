package com.with.with.post;

import com.with.with.member.CustomUser;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public Post createPost(PostDto postDto, Authentication authentication){
        CustomUser user = (CustomUser) authentication.getPrincipal();

        Post newPost = new Post();
        newPost.setStartPoint(postDto.getStartPoint());
        newPost.setEndPoint(postDto.getEndPoint());
        newPost.setDate(postDto.getDate());
        newPost.setTime(postDto.getTime());
        newPost.setPersonnel(postDto.getPersonnel());
        newPost.setWriter(user.getUsername());

        Post savedPost = postRepository.save(newPost);
        return savedPost;
    }

    public Post updatePost(PostDto postDto, Long id, Authentication authentication) {
        Optional<Post> existingPost = postRepository.findById(id);
        if (existingPost.isPresent()) {
            Post post = existingPost.get();

            post.setStartPoint(postDto.getStartPoint());
            post.setEndPoint(postDto.getEndPoint());
            post.setDate(postDto.getDate());
            post.setTime(postDto.getTime());
            post.setPersonnel(postDto.getPersonnel());

            CustomUser user = (CustomUser) authentication.getPrincipal();
            post.setWriter(user.getUsername());

            return postRepository.save(post);
        } else {
            throw new IllegalArgumentException("수정에 실패했습니다");
        }
    }

    public ResponseEntity<String> deletePost(Long id, Authentication authentication) {
        Optional<Post> post = postRepository.findById(id);
        if (!post.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시물을 찾을 수 없습니다.");
        }

        String loggedInUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
        String postOwner = post.get().getWriter();

        if (loggedInUsername != postOwner ) {
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
