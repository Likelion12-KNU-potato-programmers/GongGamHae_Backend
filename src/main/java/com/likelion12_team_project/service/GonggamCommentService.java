package com.likelion12_team_project.service;

import com.likelion12_team_project.dto.request.GonggamCommentRequest;
import com.likelion12_team_project.dto.response.GonggamCommentResponse;
import com.likelion12_team_project.dto.response.UserInfoResponse;
import com.likelion12_team_project.entity.GonggamComment;
import com.likelion12_team_project.entity.GonggamPost;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.repository.GonggamCommentRepository;
import com.likelion12_team_project.repository.GonggamPostRepository;
import com.likelion12_team_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GonggamCommentService {

    @Autowired
    private GonggamCommentRepository gonggamCommentRepository;

    @Autowired
    private GonggamPostRepository gonggamPostRepository;

    @Autowired
    private UserRepository userRepository;

    public GonggamCommentResponse addComment(GonggamCommentRequest commentRequest, Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        GonggamPost post = gonggamPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));

        GonggamComment comment = new GonggamComment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(commentRequest.getContent());

        GonggamComment savedComment = gonggamCommentRepository.save(comment);
        post.updateCommentCount(); // 댓글 수 업데이트
        gonggamPostRepository.save(post); // 업데이트된 댓글 수 저장

        return convertToDto(savedComment);
    }

    public GonggamCommentResponse updateComment(Long commentId, GonggamCommentRequest commentRequest, Long userId) {
        GonggamComment comment = gonggamCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }

        comment.setContent(commentRequest.getContent());
        GonggamComment updatedComment = gonggamCommentRepository.save(comment);

        return convertToDto(updatedComment);
    }

    public void deleteComment(Long commentId, Long userId) {
        GonggamComment comment = gonggamCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }

        GonggamPost post = comment.getPost();
        gonggamCommentRepository.delete(comment);
        post.updateCommentCount(); // 댓글 수 업데이트
        gonggamPostRepository.save(post); // 업데이트된 댓글 수 저장
    }

    private GonggamCommentResponse convertToDto(GonggamComment comment) {
        return new GonggamCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                new UserInfoResponse(comment.getUser().getId(), comment.getUser().getUserAccount(), comment.getUser().getNickname(), comment.getUser().getProfileImageUrl())
        );
    }
}
