package com.likelion12_team_project.service;

import com.likelion12_team_project.dto.request.JayuCommentRequest;
import com.likelion12_team_project.dto.response.JayuCommentResponse;
import com.likelion12_team_project.dto.response.UserResponse;
import com.likelion12_team_project.entity.JayuComment;
import com.likelion12_team_project.entity.JayuPost;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.repository.JayuCommentRepository;
import com.likelion12_team_project.repository.JayuPostRepository;
import com.likelion12_team_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JayuCommentService {

    @Autowired
    private JayuCommentRepository jayuCommentRepository;

    @Autowired
    private JayuPostRepository jayuPostRepository;

    @Autowired
    private UserRepository userRepository;

    public JayuCommentResponse addComment(JayuCommentRequest commentRequest, Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        JayuPost post = jayuPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));

        JayuComment comment = new JayuComment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(commentRequest.getContent());

        JayuComment savedComment = jayuCommentRepository.save(comment);
        post.updateCommentCount(); // 댓글 수 업데이트
        jayuPostRepository.save(post); // 업데이트된 댓글 수 저장

        return convertToDto(savedComment);
    }

    public JayuCommentResponse updateComment(Long commentId, JayuCommentRequest commentRequest, Long userId) {
        JayuComment comment = jayuCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }

        comment.setContent(commentRequest.getContent());
        JayuComment updatedComment = jayuCommentRepository.save(comment);

        return convertToDto(updatedComment);
    }

    public void deleteComment(Long commentId, Long userId) {
        JayuComment comment = jayuCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }

        JayuPost post = comment.getPost();
        jayuCommentRepository.delete(comment);
        post.updateCommentCount(); // 댓글 수 업데이트
        jayuPostRepository.save(post); // 업데이트된 댓글 수 저장
    }

    private JayuCommentResponse convertToDto(JayuComment comment) {
        return new JayuCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                new UserResponse(comment.getUser().getId(), comment.getUser().getUserid(), comment.getUser().getNickname(), comment.getUser().getProfileImageUrl())
        );
    }
}
