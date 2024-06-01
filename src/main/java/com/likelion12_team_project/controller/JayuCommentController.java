package com.likelion12_team_project.controller;

import com.likelion12_team_project.dto.request.JayuCommentRequest;
import com.likelion12_team_project.dto.response.JayuCommentResponse;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.service.JayuCommentService;
import com.likelion12_team_project.util.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/jayucomments")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class JayuCommentController {

    @Autowired
    private JayuCommentService jayuCommentService;

    @PostMapping("/{postId}")
    public ResponseEntity<JayuCommentResponse> addComment(@PathVariable("postId") Long postId, @RequestBody JayuCommentRequest commentRequest, HttpServletRequest request) {

    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        JayuCommentResponse comment = jayuCommentService.addComment(commentRequest, user.getId(), postId);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @PutMapping("/{postId}/{commentId}")
    public ResponseEntity<JayuCommentResponse> updateComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, @RequestBody JayuCommentRequest commentRequest, HttpServletRequest request) {

    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        JayuCommentResponse comment = jayuCommentService.updateComment(commentId, commentRequest, user.getId());
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{postId}/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, HttpServletRequest request) {

    	ResponseEntity<User> userResponse = SessionUtils.getCurrentUser(request);
    	if (userResponse.getStatusCode() != HttpStatus.OK) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	User user = userResponse.getBody();
    	
        jayuCommentService.deleteComment(commentId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
