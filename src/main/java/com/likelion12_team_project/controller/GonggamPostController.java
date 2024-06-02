package com.likelion12_team_project.controller;

import com.likelion12_team_project.dto.request.GonggamPostRequest;
import com.likelion12_team_project.dto.response.GonggamPostResponse;
import com.likelion12_team_project.dto.response.SidePostResponse;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.service.GonggamPostService;
import com.likelion12_team_project.util.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/gonggamposts")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class GonggamPostController {

    @Autowired
    private GonggamPostService gonggamPostService;

    @GetMapping
    public List<GonggamPostResponse> getAllPosts() {
        return gonggamPostService.getAllPosts();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<GonggamPostResponse> getPostById(@PathVariable("postId") Long postId) {
        Optional<GonggamPostResponse> post = gonggamPostService.getPostById(postId);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/best")
    public List<GonggamPostResponse> getBestPosts() {
        return gonggamPostService.getBestPosts();
    }
    
    @GetMapping("/sideposts")
    public List<SidePostResponse> getSidePosts() {
        return gonggamPostService.getSidePosts();
    }

    @PostMapping
    public ResponseEntity<GonggamPostResponse> createPost(
            @RequestPart(name = "post", required = true) GonggamPostRequest postRequest,
            @RequestPart(name = "imageFile", required = false) MultipartFile file,
            HttpServletRequest request) throws IOException {
    	
    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        GonggamPostResponse createdPost = gonggamPostService.createPost(postRequest, file, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<GonggamPostResponse> updatePost(
            @PathVariable("postId") Long postId,
            @RequestPart(name = "post", required = true) GonggamPostRequest postRequest,
            @RequestPart(name = "imageFile", required = false) MultipartFile file,
            HttpServletRequest request) throws IOException {
        
        ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
    	GonggamPostResponse updatedPost = gonggamPostService.updatePost(postId, postRequest, file, user.getId());
        
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId, 
    		HttpServletRequest request) {
    	
    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        gonggamPostService.deletePost(postId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable("postId") Long postId, HttpServletRequest request) {
    	
    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        gonggamPostService.likePost(postId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{postId}/dislike")
    public ResponseEntity<Void> dislikePost(@PathVariable("postId") Long postId, HttpServletRequest request) {
    	
    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        gonggamPostService.dislikePost(postId, user.getId());
        return ResponseEntity.ok().build();
    }
}
