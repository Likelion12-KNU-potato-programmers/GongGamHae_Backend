package com.likelion12_team_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JayuCommentResponse {
	
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserInfoResponse user;
}
