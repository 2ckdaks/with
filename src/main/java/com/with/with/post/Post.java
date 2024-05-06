package com.with.with.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@ToString
//검색성능 향상을 위해 index 생성
@Table(indexes = @Index(name = "startPointIndex", columnList = "startPoint"))
@Getter
@Setter
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String startPoint;

    @Column(nullable = false)
    private double startLatitude; // 출발지 위도 변경

    @Column(nullable = false)
    private double startLongitude; // 출발지 경도 변경

    @Column(nullable = false)
    private String endPoint;

    @Column(nullable = false)
    private double endLatitude; // 도착지 위도 변경

    @Column(nullable = false)
    private double endLongitude; // 도착지 경도 변경

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private Integer personnel;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer currentParticipants = 0;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}

