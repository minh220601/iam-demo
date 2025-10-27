package com.demo.iam_demo.service;

import com.demo.iam_demo.dto.request.LoginRequest;
import com.demo.iam_demo.dto.request.RegisterRequest;
import com.demo.iam_demo.dto.request.TokenRefreshRequest;
import com.demo.iam_demo.dto.response.LoginResponse;
import com.demo.iam_demo.dto.response.TokenRefreshResponse;
import com.demo.iam_demo.dto.response.UserResponse;
import com.demo.iam_demo.exception.AppException;
import com.demo.iam_demo.exception.ErrorCode;
import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.model.User;
import com.demo.iam_demo.repository.RoleRepository;
import com.demo.iam_demo.repository.UserRepository;
import com.demo.iam_demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
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
    private final EmailService emailService;
    private final UserActivityLogService userActivityLogService;

    // đăng ký
    public UserResponse register(RegisterRequest request){
        // kiểm tra email đã tồn tại chưa
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // mã hóa password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // gán role mặc định = ROLE_USER
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Default role not found"));

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

    // khởi tạo đăng nhập (gửi OTP hoặc tạo token trực tiếp nếu là admin
    public Object initLogin(LoginRequest request){
        // kiểm tra email tồn tại
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // kiểm tra password
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // bỏ qua opt cho admin mặc định
        if(user.getEmail().equalsIgnoreCase("admin123@gmail.com")){
            // tạo access và refresh token
            String accessToken = jwtUtils.generateAccessToken(user.getEmail());
            String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

            // lưu refresh token vào redis
            redisTemplate.opsForValue().set(
                    "refresh_token:" + user.getEmail(),
                    refreshToken,
                    jwtUtils.getRefreshTokenExpiration(),
                    TimeUnit.MILLISECONDS
            );

            // ghi log login
            userActivityLogService.log(user.getId(), "LOGIN_ADMIN");

            //trả token trực tiếp
            return new LoginResponse(accessToken, refreshToken);
        }

        // tạo OTP
        int otp = (int) (Math.random() * 900000) + 100000;
        String otpCode = String.valueOf(otp);

        // lưu OTP vào redis 5 phút
        redisTemplate.opsForValue().set(
                "login_otp:" + user.getEmail(),
                otpCode,
                5, TimeUnit.MINUTES
        );

        // gửi email
        emailService.sendLoginOtp(user.getEmail(), otpCode);

        return "OTP send to email. Verify to complete login.";
    }

    // xác thực OTP đăng nhập
    public LoginResponse verifyLoginOtp(String email, String otp){
        // xác thực OTP và cấp token
        String key = "login_otp:" + email.toLowerCase();
        String storedOtp = redisTemplate.opsForValue().get(key);

        if(storedOtp == null){
            throw new AppException(ErrorCode.UNAUTHENTICATED, "OTP expired or not found");
        }
        if(!storedOtp.equals(otp)){
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Invalid OTP");
        }

        // xóa OTP sau khi xác thực
        redisTemplate.delete(key);

        // tìm user và sinh JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        // ghi log đăng nhập
        userActivityLogService.log(user.getId(), "LOGIN");

        // lưu refresh token vào redis (key = email, value = token)
        redisTemplate.opsForValue().set(
                "refresh_token:" + user.getEmail(),
                refreshToken,
                jwtUtils.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS // thời gian sống = refresh-exp-seconds
        );

        // trả response
        return new LoginResponse(accessToken, refreshToken);
    }

    // refresh token
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request){
        String refreshToken = request.getRefreshToken();

        // kiểm tra refresh token hợp lệ
        if(!jwtUtils.validateToken(refreshToken)){
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Invalid refresh token");
        }

        // lấy subject từ refresh token (email)
        String email = jwtUtils.extractSubject(refreshToken);

        // lấy refresh token từ redis để kiểm tra
        String storedToken = redisTemplate.opsForValue().get("refresh_token:" + email);
        if(storedToken == null || !storedToken.equals(refreshToken)){
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Refresh token not found or expired");
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
    }

    // đăng xuất
    public void logout(String email, String accessToken){
        //xóa refresh token trong redis
        redisTemplate.delete("refresh_token:" + email);

        // ghi log logout
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userActivityLogService.log(user.getId(), "LOGOUT");

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
    }

    // yêu cầu reset password
    public void requestPasswordReset(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // sinh otp 6 số
        int otp = (int) (Math.random() * 900000) + 100000;
        String otpCode = String.valueOf(otp);

        // lưu vào redis
        redisTemplate.opsForValue().set(
                "reset_otp:" + email.toLowerCase(),
                otpCode,
                5, TimeUnit.MINUTES
        );

        // gửi email otp
        emailService.sendResetPasswordEmail(user.getEmail(), otpCode);
    }

    // thực hiện reset password
    public void resetPassword(String email, String otp, String newPassword){
        String key = "reset_otp:" + email.trim().toLowerCase();
        String storedOtp = redisTemplate.opsForValue().get(key);

        if(storedOtp == null){
            throw new AppException(ErrorCode.UNAUTHENTICATED, "OTP expired or not found");
        }

        if(!storedOtp.equals(otp)){
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Invalid OTP");
        }

        // xóa otp sau khi dùng
        redisTemplate.delete(key);

        // cập nhật mật khẩu mới
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // ghi log reset password
        userActivityLogService.log(user.getId(), "RESET_PASSWORD");
    }
}
