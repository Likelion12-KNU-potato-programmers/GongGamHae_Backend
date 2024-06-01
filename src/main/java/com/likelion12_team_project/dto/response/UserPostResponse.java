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
public class UserPostResponse {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
}
