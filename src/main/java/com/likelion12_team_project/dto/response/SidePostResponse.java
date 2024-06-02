package com.likelion12_team_project.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SidePostResponse {
	
	private String category;
    private List<GonggamPostResponse> posts;
}
