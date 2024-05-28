package com.likelion12_team_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gonggam_post_likes")
public class GonggamPostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private GonggamPost post;

    private boolean isLike; // true: 좋아요, false: 싫어요

    public GonggamPostLike() {}

    public GonggamPostLike(User user, GonggamPost post, boolean isLike) {
        this.user = user;
        this.post = post;
        this.isLike = isLike;
    }
}
