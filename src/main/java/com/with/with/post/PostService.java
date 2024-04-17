package com.with.with.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    @Transactional
    public Post createPost(PostDto postDto){

        Post newPost = new Post();
//        newPost.setStartPoint(postDto.getStartPoint());
//        newPost.setEndPoint(postDto.getEndPoint());
//        newPost.setDate(postDto.getDate());
//        newPost.setTime(postDto.getTime());
//        newPost.setPersonnel(postDto.getPersonnel());
//        newPost.setWriter(postDto.getWriter());

        // 하드코딩된 값으로 설정
        newPost.setStartPoint("서울역");
        newPost.setEndPoint("부산역");
        newPost.setDate(LocalDate.of(2024, 4, 15));
        newPost.setTime(LocalTime.of(13, 30));
        newPost.setPersonnel(4);
        newPost.setWriter("홍길동");

        Post savedPost = postRepository.save(newPost);
        return savedPost;
    }
}
