package com.likelion12_team_project.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {
	
    private MultipartFile profileImage;
}
