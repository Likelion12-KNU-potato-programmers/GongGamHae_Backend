package com.likelion12_team_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserResponse {
	
    private Long id;
    private String userid;
    private String nickname;
    private String profileImage;
}
