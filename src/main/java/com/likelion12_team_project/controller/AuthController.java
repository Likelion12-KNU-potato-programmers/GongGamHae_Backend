package com.likelion12_team_project.controller;

import com.likelion12_team_project.dto.request.LoginRequest;
import com.likelion12_team_project.dto.request.RegisterRequest;
import com.likelion12_team_project.entity.User;
import com.likelion12_team_project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestPart("user") RegisterRequest request,
                                           @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            if (profileImage != null) {
                request.setProfileImage(profileImage);
            }
            authService.register(request);
            return ResponseEntity.status(HttpStatus.OK).body("User registered successfully");
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            User user = authService.login(request);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("user", user);
            return ResponseEntity.ok().body("User logged in successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().body("User logged out successfully");
    }
}
