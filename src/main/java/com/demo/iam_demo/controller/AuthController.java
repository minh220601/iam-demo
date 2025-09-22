package com.demo.iam_demo.controller;

import com.demo.iam_demo.dto.request.LoginRequest;
import com.demo.iam_demo.dto.request.LogoutRequest;
import com.demo.iam_demo.dto.request.RegisterRequest;
import com.demo.iam_demo.dto.request.TokenRefreshRequest;
import com.demo.iam_demo.dto.response.LoginResponse;
import com.demo.iam_demo.dto.response.TokenRefreshResponse;
import com.demo.iam_demo.dto.response.UserResponse;
import com.demo.iam_demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @Valid @RequestBody LogoutRequest dto){
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            authService.logout(dto.getEmail(), accessToken);
        }
        return ResponseEntity.ok("Logout successful");
    }
}
