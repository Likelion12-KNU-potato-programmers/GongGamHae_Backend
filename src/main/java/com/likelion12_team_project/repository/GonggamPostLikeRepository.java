package com.likelion12_team_project.repository;

import com.likelion12_team_project.entity.GonggamPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GonggamPostLikeRepository extends JpaRepository<GonggamPostLike, Long> {
    Optional<GonggamPostLike> findByUserIdAndPostId(Long userId, Long postId);
}
