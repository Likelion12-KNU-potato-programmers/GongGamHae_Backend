package com.likelion12_team_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GonggamPostResponse implements UserPostResponse{

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private UserInfoResponse userInfo;
    private int commentCount;
    private List<GonggamCommentResponse> comments;
    private int likes;  // 좋아요 수
    private int dislikes;  // 싫어요 수
    
    @Override
    public Long getId() {
        return id;
    }
}
