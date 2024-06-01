package com.likelion12_team_project.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.likelion12_team_project.dto.request.UserNicknameUpdateRequest;
import com.likelion12_team_project.dto.request.UserProfileUpdateRequest;
import com.likelion12_team_project.dto.response.GonggamCommentResponse;
import com.likelion12_team_project.dto.response.GonggamPostResponse;
import com.likelion12_team_project.dto.response.JayuCommentResponse;
import com.likelion12_team_project.dto.response.JayuPostResponse;
import com.likelion12_team_project.dto.response.UserInfoResponse;
import com.likelion12_team_project.dto.response.UserPostResponse;
import com.likelion12_team_project.entity.GonggamPost;
import com.likelion12_team_project.entity.JayuPost;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
                .map(this::returnTheJayuPost)
                .collect(Collectors.toList());

        List<UserPostResponse> gonggamPosts = gonggamPostRepository.findByUserId(userId).stream()
                .map(this::returnTheGonggamPost)
                .collect(Collectors.toList());

        List<UserPostResponse> userPosts = new ArrayList<>();
        userPosts.addAll(jayuPosts);
        userPosts.addAll(gonggamPosts);

        // 중복 제거
        Map<Long, UserPostResponse> uniquePosts = userPosts.stream()
                .collect(Collectors.toMap(UserPostResponse::getId, Function.identity(), (existing, replacement) -> existing));

        return new ArrayList<>(uniquePosts.values());
    }

    public List<UserPostResponse> getUserCommentedPosts(Long userId) {
        List<UserPostResponse> jayuCommentedPosts = jayuCommentRepository.findByUserId(userId).stream()
                .map(comment -> returnTheJayuPost(comment.getPost()))
                .collect(Collectors.toList());

        List<UserPostResponse> gonggamCommentedPosts = gonggamCommentRepository.findByUserId(userId).stream()
                .map(comment -> returnTheGonggamPost(comment.getPost()))
                .collect(Collectors.toList());

        List<UserPostResponse> commentedPosts = new ArrayList<>();
        commentedPosts.addAll(jayuCommentedPosts);
        commentedPosts.addAll(gonggamCommentedPosts);

        // postId로 중복 제거
        Map<Long, UserPostResponse> uniquePosts = commentedPosts.stream()
                .collect(Collectors.toMap(UserPostResponse::getId, Function.identity(), (existing, replacement) -> existing));

        return new ArrayList<>(uniquePosts.values());
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        return new UserInfoResponse(user.getId(), user.getUserAccount(), user.getNickname(), user.getProfileImageUrl());
    }

    private JayuPostResponse returnTheJayuPost(JayuPost post) {
    	User user = post.getUser();
    	UserInfoResponse userInfo = new UserInfoResponse(user.getId(), user.getUserAccount(), user.getNickname(), user.getProfileImageUrl());

    	List<JayuCommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> new JayuCommentResponse(comment.getId(), comment.getContent(), comment.getCreatedAt(), userInfo))
                .collect(Collectors.toList());
        return new JayuPostResponse(post.getId(), post.getTitle(), post.getContent(), post.getImageUrl(), post.getCreatedAt(), userInfo, 
                                    post.getCommentCount(), commentResponses);
    }

    private GonggamPostResponse returnTheGonggamPost(GonggamPost post) {
    	User user = post.getUser();
    	UserInfoResponse userInfo = new UserInfoResponse(user.getId(), user.getUserAccount(), user.getNickname(), user.getProfileImageUrl());
    	
    	List<GonggamCommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> new GonggamCommentResponse(comment.getId(), comment.getContent(), comment.getCreatedAt(), userInfo))
                .collect(Collectors.toList());
        return new GonggamPostResponse(post.getId(), post.getTitle(), post.getContent(), post.getImageUrl(), post.getCreatedAt(), userInfo, 
                                       post.getCommentCount(), commentResponses, post.getLikes(), post.getDislikes());
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
