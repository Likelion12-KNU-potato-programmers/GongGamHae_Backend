package com.likelion12_team_project.repository;

import com.likelion12_team_project.entity.GonggamPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GonggamPostRepository extends JpaRepository<GonggamPost, Long>{
    List<GonggamPost> findByLikesGreaterThan(int likes);
    List<GonggamPost> findByUserId(Long userId);
}
