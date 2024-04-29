package com.with.with.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class PostDto {
    private String startPoint;
    private double startLatitude;
    private double startLongitude;
    private String endPoint;
    private double endLatitude;
    private double endLongitude;
    private LocalDate date;
    private LocalTime time;
    private Integer personnel;
}
