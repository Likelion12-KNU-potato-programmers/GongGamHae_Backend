package com.likelion12_team_project.controller;

import com.likelion12_team_project.dto.request.UserNicknameUpdateRequest;
import com.likelion12_team_project.dto.request.UserProfileUpdateRequest;
import com.likelion12_team_project.dto.response.UserCommentedPostResponse;
import com.likelion12_team_project.dto.response.UserPostResponse;
import com.likelion12_team_project.dto.response.UserResponse;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserPostResponse> userPosts = userService.getUserPosts(user.getId());
        return ResponseEntity.ok(userPosts);
    }

    @GetMapping("/me/commented-posts")
    public ResponseEntity<List<UserCommentedPostResponse>> getCurrentUserCommentedPosts(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserCommentedPostResponse> userCommentedPosts = userService.getUserCommentedPosts(user.getId());
        return ResponseEntity.ok(userCommentedPosts);
    }

    @PutMapping("/me/nickname")
    public ResponseEntity<String> updateNickname(HttpServletRequest request, @RequestBody UserNicknameUpdateRequest nicknameRequest) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.updateNickname(user.getId(), nicknameRequest);
        return ResponseEntity.ok("Nickname updated successfully");
    }

    @PutMapping("/me/profile")
    public ResponseEntity<String> updateProfile(HttpServletRequest request, 
                                                @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
    public ResponseEntity<UserResponse> getCurrentUserInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse userResponse = userService.getUserInfo(user.getId());
        return ResponseEntity.ok(userResponse);
    }
}
