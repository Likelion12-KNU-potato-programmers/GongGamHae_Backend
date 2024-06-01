package com.likelion12_team_project.controller;

import com.likelion12_team_project.dto.request.UserNicknameUpdateRequest;
import com.likelion12_team_project.dto.request.UserProfileUpdateRequest;
import com.likelion12_team_project.dto.response.UserInfoResponse;
import com.likelion12_team_project.dto.response.UserPostResponse;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.service.UserService;
import com.likelion12_team_project.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me/posts")
    public ResponseEntity<List<UserPostResponse>> getCurrentUserPosts(HttpServletRequest request) {
        ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
        if (userResponse.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userResponse.getBody();

        List<UserPostResponse> userPosts = userService.getUserPosts(user.getId());
        return ResponseEntity.ok(userPosts);
    }

    @GetMapping("/me/commented-posts")
    public ResponseEntity<List<UserPostResponse>> getCurrentUserCommentedPosts(HttpServletRequest request) {
        ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
        if (userResponse.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userResponse.getBody();

        List<UserPostResponse> userCommentedPosts = userService.getUserCommentedPosts(user.getId());
        return ResponseEntity.ok(userCommentedPosts);
    }

    @PutMapping("/me/nickname")
    public ResponseEntity<String> updateNickname(HttpServletRequest request, @RequestBody UserNicknameUpdateRequest nicknameRequest) {
        
    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        userService.updateNickname(user.getId(), nicknameRequest);
        return ResponseEntity.ok("Nickname updated successfully");
    }

    @PutMapping("/me/profile")
    public ResponseEntity<String> updateProfile(HttpServletRequest request, 
                                                @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        try {
            UserProfileUpdateRequest profileUpdateRequest = new UserProfileUpdateRequest();
            profileUpdateRequest.setProfileImage(profileImage);
            userService.updateProfile(user.getId(), profileUpdateRequest);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update profile");
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUserInfo(HttpServletRequest request) {

    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        UserInfoResponse userInfoResponse = userService.getUserInfo(user.getId());
        return ResponseEntity.ok(userInfoResponse);
    }
}
