package com.with.with.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // String 타입의 target ID를 가진 모든 리뷰를 찾는 메서드
    List<Review> findAllByTarget(String targetId);
}
