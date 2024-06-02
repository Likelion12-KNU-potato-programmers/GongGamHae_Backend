package com.likelion12_team_project.repository;

import com.likelion12_team_project.entity.GonggamPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GonggamPostRepository extends JpaRepository<GonggamPost, Long>{
	List<GonggamPost> findByLikesGreaterThan(int likes);
    List<GonggamPost> findByUserId(Long userId);

    // 최근에 작성된 3개의 포스트를 가져오는 쿼리 메소드
    List<GonggamPost> findTop3ByOrderByCreatedAtDesc();

}
