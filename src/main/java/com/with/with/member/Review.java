package com.with.with.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String review;

    @Column
    private String writer;

    @Column
    private String target;
}
