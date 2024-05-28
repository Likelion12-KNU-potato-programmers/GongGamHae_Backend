package com.likelion12_team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion12_team_project.entity.GonggamComment;

@Repository
public interface GonggamCommentRepository extends JpaRepository<GonggamComment, Long>{
    List<GonggamComment> findByUserId(Long userId);
    List<GonggamComment> findByPostId(Long postId);
}
