package com.likelion12_team_project.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.likelion12_team_project.dto.request.GonggamPostRequest;
import com.likelion12_team_project.dto.response.GonggamCommentResponse;
import com.likelion12_team_project.dto.response.GonggamPostResponse;
import com.likelion12_team_project.dto.response.UserInfoResponse;
import com.likelion12_team_project.entity.GonggamComment;
import com.likelion12_team_project.entity.GonggamPost;
import com.likelion12_team_project.entity.GonggamPostLike;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.repository.GonggamCommentRepository;
import com.likelion12_team_project.repository.GonggamPostLikeRepository;
import com.likelion12_team_project.repository.GonggamPostRepository;
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
public class GonggamPostService {

    @Autowired
    private GonggamPostRepository gonggamPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private GonggamCommentRepository gonggamCommentRepository;

    @Autowired
    private GonggamPostLikeRepository gonggamPostLikeRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public List<GonggamPostResponse> getAllPosts() {
        return gonggamPostRepository.findAll().stream().map(this::convertToDtoWithComments).collect(Collectors.toList());
    }

    public Optional<GonggamPostResponse> getPostById(Long id) {
        return gonggamPostRepository.findById(id).map(this::convertToDtoWithComments);
    }

    public List<GonggamPostResponse> getBestPosts() {
        return gonggamPostRepository.findByLikesGreaterThan(10).stream().map(this::convertToDtoWithComments).collect(Collectors.toList());
    }

    public GonggamPostResponse createPost(GonggamPostRequest postRequest, MultipartFile image, Long userid) throws IOException {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        GonggamPost post = new GonggamPost();
        post.setUser(user);
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadFile(image);
            post.setImageUrl(imageUrl);
        }
        post.updateCommentCount(); // 댓글 수 업데이트
        GonggamPost savedPost = gonggamPostRepository.save(post);
        return convertToDtoWithComments(savedPost);
    }

    public GonggamPostResponse updatePost(Long postId, GonggamPostRequest postRequest, MultipartFile image) throws IOException {
        GonggamPost post = gonggamPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadFile(image);
            post.setImageUrl(imageUrl);
        }
        post.updateCommentCount(); // 댓글 수 업데이트
        GonggamPost updatedPost = gonggamPostRepository.save(post);
        return convertToDtoWithComments(updatedPost);
    }

    public void deletePost(Long postId) {
        GonggamPost post = gonggamPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));
        gonggamPostRepository.delete(post);
    }

    public void likePost(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        GonggamPost post = gonggamPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));

        Optional<GonggamPostLike> existingLike = gonggamPostLikeRepository.findByUserIdAndPostId(userId, postId);
        if (existingLike.isPresent()) {
            GonggamPostLike like = existingLike.get();
            if (!like.isLike()) {
                like.setLike(true);
                post.setLikes(post.getLikes() + 1);
                post.setDislikes(post.getDislikes() - 1);
                gonggamPostLikeRepository.save(like);
                gonggamPostRepository.save(post);
            }
        } else {
            GonggamPostLike newLike = new GonggamPostLike(user, post, true);
            post.setLikes(post.getLikes() + 1);
            gonggamPostLikeRepository.save(newLike);
            gonggamPostRepository.save(post);
        }
    }

    public void dislikePost(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        GonggamPost post = gonggamPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID"));

        Optional<GonggamPostLike> existingLike = gonggamPostLikeRepository.findByUserIdAndPostId(userId, postId);
        if (existingLike.isPresent()) {
            GonggamPostLike like = existingLike.get();
            if (like.isLike()) {
                like.setLike(false);
                post.setLikes(post.getLikes() - 1);
                post.setDislikes(post.getDislikes() + 1);
                gonggamPostLikeRepository.save(like);
                gonggamPostRepository.save(post);
            }
        } else {
            GonggamPostLike newLike = new GonggamPostLike(user, post, false);
            post.setDislikes(post.getDislikes() + 1);
            gonggamPostLikeRepository.save(newLike);
            gonggamPostRepository.save(post);
        }
    }

    public List<GonggamPostResponse> getPostsByUser(Long userId) {
        return gonggamPostRepository.findByUserId(userId).stream().map(this::convertToDtoWithComments).collect(Collectors.toList());
    }

    private GonggamPostResponse convertToDtoWithComments(GonggamPost post) {
        User user = post.getUser();
        UserInfoResponse userResponse = new UserInfoResponse(user.getId(), user.getUserAccount(), user.getNickname(), user.getProfileImageUrl());
        List<GonggamComment> comments = gonggamCommentRepository.findByPostId(post.getId());
        List<GonggamCommentResponse> commentResponses = comments.stream()
                .map(this::convertCommentToDto)
                .collect(Collectors.toList());
        post.updateCommentCount(); // 댓글 수 업데이트
        return new GonggamPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                userResponse,
                post.getCommentCount(), // 댓글 수 반환
                commentResponses, // 댓글 목록 추가
                post.getLikes(), // 좋아요 수 반환
                post.getDislikes() // 싫어요 수 반환
        );
    }

    private GonggamCommentResponse convertCommentToDto(GonggamComment comment) {
        User user = comment.getUser();
        UserInfoResponse userResponse = new UserInfoResponse(user.getId(), user.getUserAccount(), user.getNickname(), user.getProfileImageUrl());
        return new GonggamCommentResponse(comment.getId(), comment.getContent(), comment.getCreatedAt(), userResponse);
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
