package com.likelion12_team_project.controller;

import com.likelion12_team_project.dto.request.JayuPostRequest;
import com.likelion12_team_project.dto.response.JayuPostResponse;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.service.JayuPostService;
import com.likelion12_team_project.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jayuposts")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class JayuPostController {

    @Autowired
    private JayuPostService jayuPostService;

    @GetMapping
    public List<JayuPostResponse> getAllPosts() {
        return jayuPostService.getAllPosts();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<JayuPostResponse> getPostById(@PathVariable("postId") Long postId) {
        Optional<JayuPostResponse> post = jayuPostService.getPostById(postId);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JayuPostResponse> createPost(
            @RequestPart(name = "post", required = true) JayuPostRequest postRequest,
            @RequestPart(name = "imageFile", required = false) MultipartFile file, 
            HttpServletRequest request) throws IOException {
    	
    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        JayuPostResponse createdPost = jayuPostService.createPost(postRequest, file, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<JayuPostResponse> updatePost(
            @PathVariable("postId") Long postId,
            @RequestPart(name = "post", required = true) JayuPostRequest postRequest,
            @RequestPart(name = "imageFile", required = false) MultipartFile file) throws IOException {
        JayuPostResponse updatedPost = jayuPostService.updatePost(postId, postRequest, file);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        jayuPostService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}
