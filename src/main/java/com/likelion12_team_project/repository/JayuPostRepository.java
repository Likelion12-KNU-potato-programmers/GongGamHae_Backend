package com.likelion12_team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion12_team_project.entity.JayuPost;

@Repository
public interface JayuPostRepository extends JpaRepository<JayuPost, Long>{
	
	List<JayuPost> findByUserId(Long userId);
}
