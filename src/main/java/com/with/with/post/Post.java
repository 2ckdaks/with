package com.with.with.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String startPoint;

    @Column(nullable = false)
    private String endPoint;

    @Column(nullable = false)
    private LocalDate date;  // 날짜만 필요할 경우 LocalDate 사용

    @Column(nullable = false)
    private LocalTime time;  // 시간만 필요할 경우 LocalTime 사용

    @Column(nullable = false)
    private Integer personnel;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false, updatable = false)  // 생성 시에만 설정, 업데이트 불가
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();  // `this` 키워드를 사용하여 클래스 필드를 참조
        this.updatedAt = LocalDateTime.now();  // 동시에 updatedAt도 초기화
    }
}
