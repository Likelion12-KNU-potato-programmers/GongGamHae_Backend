package com.likelion12_team_project.controller;

import com.likelion12_team_project.dto.request.GonggamCommentRequest;
import com.likelion12_team_project.dto.response.GonggamCommentResponse;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.service.GonggamCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/gonggamcomments")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class GonggamCommentController {

    @Autowired
    private GonggamCommentService gonggamCommentService;

    @PostMapping("/{postId}")
    public ResponseEntity<GonggamCommentResponse> addComment(@PathVariable("postId") Long postId, @RequestBody GonggamCommentRequest commentRequest, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        GonggamCommentResponse comment = gonggamCommentService.addComment(commentRequest, user.getId(), postId);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @PutMapping("/{postId}/{commentId}")
    public ResponseEntity<GonggamCommentResponse> updateComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, @RequestBody GonggamCommentRequest commentRequest, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        GonggamCommentResponse comment = gonggamCommentService.updateComment(commentId, commentRequest, user.getId());
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{postId}/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        gonggamCommentService.deleteComment(commentId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
