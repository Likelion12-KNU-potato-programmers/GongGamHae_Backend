package com.likelion12_team_project.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.likelion12_team_project.dto.request.UserNicknameUpdateRequest;
import com.likelion12_team_project.dto.request.UserProfileUpdateRequest;
import com.likelion12_team_project.dto.response.UserCommentedPostResponse;
import com.likelion12_team_project.dto.response.UserPostResponse;
import com.likelion12_team_project.dto.response.UserResponse;
import com.likelion12_team_project.entity.GonggamPost;
import com.likelion12_team_project.entity.JayuPost;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JayuPostRepository jayuPostRepository;

    @Autowired
    private JayuCommentRepository jayuCommentRepository;

    @Autowired
    private GonggamPostRepository gonggamPostRepository;

    @Autowired
    private GonggamCommentRepository gonggamCommentRepository;
    
    @Autowired
    private AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public User findByUserid(String userid) {
        return userRepository.findByUserid(userid);
    }

    public void updateNickname(Long userId, UserNicknameUpdateRequest nicknameRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        user.setNickname(nicknameRequest.getNickname());
        userRepository.save(user);
    }

    public void updateProfile(Long userId, UserProfileUpdateRequest request) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            // 프로필 이미지를 S3에 업로드하는 로직 추가 (이전에 사용한 uploadFile 메서드 사용)
            String imageUrl = uploadFile(request.getProfileImage());
            user.setProfileImageUrl(imageUrl);
        }

        userRepository.save(user);
    }

    public List<UserPostResponse> getUserPosts(Long userId) {
        List<UserPostResponse> jayuPosts = jayuPostRepository.findByUserId(userId).stream()
                .map(this::convertToUserPostResponse)
                .collect(Collectors.toList());

        List<UserPostResponse> gonggamPosts = gonggamPostRepository.findByUserId(userId).stream()
                .map(this::convertToUserPostResponse)
                .collect(Collectors.toList());

        jayuPosts.addAll(gonggamPosts);
        return jayuPosts;
    }

    public List<UserCommentedPostResponse> getUserCommentedPosts(Long userId) {
        List<UserCommentedPostResponse> jayuCommentedPosts = jayuCommentRepository.findByUserId(userId).stream()
                .map(comment -> convertToUserCommentedPostResponse(comment.getPost()))
                .collect(Collectors.toList());

        List<UserCommentedPostResponse> gonggamCommentedPosts = gonggamCommentRepository.findByUserId(userId).stream()
                .map(comment -> convertToUserCommentedPostResponse(comment.getPost()))
                .collect(Collectors.toList());

        // postId를 기준으로 목록을 결합
        List<UserCommentedPostResponse> commentedPosts = jayuCommentedPosts;
        commentedPosts.addAll(gonggamCommentedPosts);

        // postId로 중복 제거
        Map<Long, UserCommentedPostResponse> uniquePosts = commentedPosts.stream()
                .collect(Collectors.toMap(UserCommentedPostResponse::getId, Function.identity(), (existing, replacement) -> existing));

        return uniquePosts.values().stream().collect(Collectors.toList());
    }

    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        return new UserResponse(user.getId(), user.getUserid(), user.getNickname(), user.getProfileImageUrl());
    }

    private UserPostResponse convertToUserPostResponse(JayuPost post) {
        return new UserPostResponse(post.getId(), post.getTitle(), post.getContent(), post.getImageUrl(), post.getCreatedAt());
    }

    private UserPostResponse convertToUserPostResponse(GonggamPost post) {
        return new UserPostResponse(post.getId(), post.getTitle(), post.getContent(), post.getImageUrl(), post.getCreatedAt());
    }

    private UserCommentedPostResponse convertToUserCommentedPostResponse(JayuPost post) {
        return new UserCommentedPostResponse(post.getId(), post.getTitle(), post.getContent(), post.getImageUrl(), post.getCreatedAt());
    }

    private UserCommentedPostResponse convertToUserCommentedPostResponse(GonggamPost post) {
        return new UserCommentedPostResponse(post.getId(), post.getTitle(), post.getContent(), post.getImageUrl(), post.getCreatedAt());
    }

    private String uploadFile(MultipartFile file) throws IOException {
        // S3 업로드 로직 추가 (기존 코드에서 uploadFile 메서드 사용)
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        s3Client.putObject(bucket, key, file.getInputStream(), metadata);
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
}
