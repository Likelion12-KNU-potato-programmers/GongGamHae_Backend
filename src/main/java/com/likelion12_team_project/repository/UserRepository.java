package com.likelion12_team_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion12_team_project.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	User findByUserAccount(String userAccount);
	User findByNickname(String nickname);
}
