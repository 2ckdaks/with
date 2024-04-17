package com.with.with.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class PostDto {
    private String startPoint;
    private String endPoint;
    private LocalDate date;
    private LocalTime time;
    private Integer personnel;
    private String writer;
}
