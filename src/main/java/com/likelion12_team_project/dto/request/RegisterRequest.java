package com.likelion12_team_project.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class RegisterRequest {
    private String userid;
    private String password;
    private String confirmPassword;  // 패스워드 확인 필드 추가
    private String nickname;
    private MultipartFile profileImage;
}
