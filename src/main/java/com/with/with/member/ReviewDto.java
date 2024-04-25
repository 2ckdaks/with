package com.with.with.member;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReviewDto {
    private String review;
    private String writer;
    private String target;  // 리뷰 대상 사용자의 ID 추가
}
