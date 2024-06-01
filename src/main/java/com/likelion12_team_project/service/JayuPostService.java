package com.likelion12_team_project.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.likelion12_team_project.dto.request.JayuPostRequest;
import com.likelion12_team_project.dto.response.JayuCommentResponse;
import com.likelion12_team_project.dto.response.JayuPostResponse;
import com.likelion12_team_project.dto.response.UserInfoResponse;
import com.likelion12_team_project.entity.JayuComment;
import com.likelion12_team_project.entity.JayuPost;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.repository.JayuCommentRepository;
import com.likelion12_team_project.repository.JayuPostRepository;
import com.likelion12_team_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JayuPostService {

    @Autowired
    private JayuPostRepository jayuPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private JayuCommentRepository jayuCommentRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public List<JayuPostResponse> getAllPosts() {
        return jayuPostRepository.findAll().stream().map(this::convertToDtoWithComments).collect(Collectors.toList());
    }

    public Optional<JayuPostResponse> getPostById(Long id) {
        return jayuPostRepository.findById(id).map(this::convertToDtoWithComments);
    }

    public JayuPostResponse createPost(JayuPostRequest postRequest, MultipartFile image, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        JayuPost post = new JayuPost();
        post.setUser(user);
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadFile(image);
            post.setImageUrl(imageUrl);
        }
        post.updateCommentCount(); // 댓글 수 업데이트
        JayuPost savedPost = jayuPostRepository.save(post);
        return convertToDtoWithComments(savedPost);
    }

    public JayuPostResponse updatePost(Long postId, JayuPostRequest postRequest, MultipartFile image, Long userId) throws IOException {
        JayuPost post = jayuPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can update this post.");
        }
        
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        if (image != null && !image.isEmpty()) {
            // 이미지가 존재하면 업로드하고 이미지 URL 업데이트
            String imageUrl = uploadFile(image);
            post.setImageUrl(imageUrl);
        } else {
            // 이미지가 없는 경우 이미지 URL을 null로 설정
            post.setImageUrl(null);
        }
        
        post.updateCommentCount(); // 댓글 수 업데이트
        JayuPost updatedPost = jayuPostRepository.save(post);
        return convertToDtoWithComments(updatedPost);
    }

    public void deletePost(Long postId, Long userId) {
        JayuPost post = jayuPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the author can update this post.");
        }
        
        jayuPostRepository.delete(post);
    }

    

    private JayuPostResponse convertToDtoWithComments(JayuPost post) {
        User user = post.getUser();
        UserInfoResponse userResponse = new UserInfoResponse(user.getId(), user.getUserAccount(), user.getNickname(), user.getProfileImageUrl());
        List<JayuComment> comments = jayuCommentRepository.findByPostId(post.getId());
        List<JayuCommentResponse> commentResponses = comments.stream()
                .map(this::convertCommentToDto)
                .collect(Collectors.toList());
        post.updateCommentCount(); // 댓글 수 업데이트
        return new JayuPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                userResponse,
                post.getCommentCount(), // 댓글 수 반환
                commentResponses // 댓글 목록 추가
        );
    }

    private JayuCommentResponse convertCommentToDto(JayuComment comment) {
        User user = comment.getUser();
        UserInfoResponse userResponse = new UserInfoResponse(user.getId(), user.getUserAccount(), user.getNickname(), user.getProfileImageUrl());
        return new JayuCommentResponse(comment.getId(), comment.getContent(), comment.getCreatedAt(), userResponse);
    }

    private String uploadFile(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        s3Client.putObject(bucket, key, file.getInputStream(), metadata);
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
}
