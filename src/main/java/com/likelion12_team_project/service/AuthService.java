package com.likelion12_team_project.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.likelion12_team_project.dto.request.LoginRequest;
import com.likelion12_team_project.dto.request.RegisterRequest;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public void register(RegisterRequest request) throws IOException {
        if (userRepository.findByUserAccount(request.getUserAccount()) != null) {
            throw new RuntimeException("ID already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        if (userRepository.findByNickname(request.getNickname()) != null) {
            throw new RuntimeException("Nickname already exists");
        }

        User user = new User();
        user.setUserAccount(request.getUserAccount());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setProfileImageUrl("https://likelion12-team-project-bucket.s3.ap-northeast-2.amazonaws.com/default_profile.png");

        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            String imageUrl = uploadFile(request.getProfileImage());
            user.setProfileImageUrl(imageUrl);
        }

        userRepository.save(user);
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByUserAccount(request.getUserAccount());
        if (user != null && user.getPassword().equals(request.getPassword())) {
            return user;
        } else {
            throw new RuntimeException("Invalid userid or password");
        }
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
