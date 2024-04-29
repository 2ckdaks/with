package com.with.with.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findPageBy(Pageable pageable);

    @Query(value = "SELECT * FROM post ORDER BY ST_Distance_Sphere(point(start_longitude, start_latitude), point(:lon, :lat))",
            countQuery = "SELECT count(*) FROM post",
            nativeQuery = true)
    Page<Post> findByLocationNear(@Param("lat") double latitude, @Param("lon") double longitude, Pageable pageable);
}
