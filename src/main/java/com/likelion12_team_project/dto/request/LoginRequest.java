package com.likelion12_team_project.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
	
	private String userAccount;
    private String password;
}
