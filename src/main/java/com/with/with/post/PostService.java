package com.with.with.post;

import com.with.with.member.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
//        newPost.setWriter(postDto.getWriter());
        newPost.setWriter(user.displayName);


        // 하드코딩된 값으로 설정
//        newPost.setStartPoint("서울역");
//        newPost.setEndPoint("부산역");
//        newPost.setDate(LocalDate.of(2024, 4, 15));
//        newPost.setTime(LocalTime.of(13, 30));
//        newPost.setPersonnel(4);
//        newPost.setWriter("홍길동");

        Post savedPost = postRepository.save(newPost);
        return savedPost;
    }
}
