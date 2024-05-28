package com.likelion12_team_project.repository;

import com.likelion12_team_project.entity.JayuComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JayuCommentRepository extends JpaRepository<JayuComment, Long> {
    List<JayuComment> findByUserId(Long userId);
    List<JayuComment> findByPostId(Long postId);
    int countByPostId(Long postId); // 댓글 수 계산을 위한 메서드 추가
}
