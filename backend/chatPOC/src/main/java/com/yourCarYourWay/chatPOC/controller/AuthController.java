package com.yourCarYourWay.chatPOC.controller;

import com.yourCarYourWay.chatPOC.dto.AuthRequest;
import com.yourCarYourWay.chatPOC.dto.AuthResponse;
import com.yourCarYourWay.chatPOC.entity.UserEntity;
import com.yourCarYourWay.chatPOC.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserEntity user) {
        return ResponseEntity.ok(authService.register(user));
    }
}
