package com.demo.iam_demo.service;

import com.demo.iam_demo.dto.request.LoginRequest;
import com.demo.iam_demo.dto.request.RegisterRequest;
import com.demo.iam_demo.dto.request.TokenRefreshRequest;
import com.demo.iam_demo.dto.response.LoginResponse;
import com.demo.iam_demo.dto.response.TokenRefreshResponse;
import com.demo.iam_demo.dto.response.UserResponse;
import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.model.User;
import com.demo.iam_demo.repository.RoleRepository;
import com.demo.iam_demo.repository.UserRepository;
import com.demo.iam_demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request){
        // kiểm tra email đã tồn tại chưa
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email is already registered");
        }

        // mã hóa password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // gán role mặc định = ROLE_USER
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encodedPassword)
                .phone(request.getPhone())
                .address(request.getAddress())
                .active(true)
                .build();

        user.getRoles().add(defaultRole);

        // lưu database
        User saved = userRepository.save(user);

        // trả DTO
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getUsername(), saved.isActive());
    }

    public LoginResponse login(LoginRequest request){
        // xác thực email + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // lấy thông tin user
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow();

        // sinh JWT
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        // lưu refresh token vào redis (key = email, value = token)
//        redisTemplate.opsForValue().set(
//                "refresh_token:" + user.getEmail(),
//                refreshToken,
//                jwtUtils.getRefreshTokenExpiration(),
//                TimeUnit.MILLISECONDS // thời gian sống = refresh-exp-seconds
//        );

        // trả response
        return new LoginResponse(accessToken, refreshToken);
    }

    /*public TokenRefreshResponse refreshToken(TokenRefreshRequest request){
        String refreshToken = request.getRefreshToken();

        // kiểm tra refresh token hợp lệ
        if(!jwtUtils.validateToken(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }

        // lấy subject từ refresh token (email)
        String email = jwtUtils.extractSubject(refreshToken);

        // lấy refresh token từ redis để kiểm tra
        String storedToken = redisTemplate.opsForValue().get("refresh_token:" + email);
        if(storedToken == null || !storedToken.equals(refreshToken)){
            throw new RuntimeException("Refresh token not found or expired");
        }

        // Sinh access token mới
        String newAccessToken = jwtUtils.generateAccessToken(email);

        // có thể cấp mới refresh token luôn để rotation
        String newRefreshToken = jwtUtils.generateRefreshToken(email);
        redisTemplate.opsForValue().set(
                "refresh_token:" + email,
                newRefreshToken,
                jwtUtils.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // trả response
        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }*/

    /*public void logout(String email, String accessToken){
        //xóa refresh token trong redis
        redisTemplate.delete("refresh_token:" + email);

        //thêm access token vào blacklist
        long expirationMillis = jwtUtils.getRemainingValidity(accessToken);
        if(expirationMillis > 0){
            redisTemplate.opsForValue().set(
                    "blacklist_token:" + accessToken,
                    "true",
                    expirationMillis,
                    TimeUnit.MILLISECONDS
            );
        }
    }*/
}
