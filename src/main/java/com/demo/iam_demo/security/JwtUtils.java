package com.demo.iam_demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private final SecretKey key;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-seconds}") long jwtExpSec,
            @Value("${jwt.refresh-exp-seconds}") long refreshExpSec
    ){
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); //Tạo key từ secret
        this.jwtExpirationMs = jwtExpSec * 1000; // đổi second -> ms
        this.refreshExpirationMs = refreshExpSec * 1000;
    }

    //sinh access token
    public String generateAccessToken(String subject){
        return Jwts.builder()
                .subject(subject)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    //sinh refresh token
    public String generateRefreshToken(String subject){
        return Jwts.builder()
                .subject(subject)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    // lấy loại token
    public String extractTokenType(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    //lấy subject (email/username) từ token
    public String extractSubject(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //kiểm tra token hợp lệ
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public long getRefreshTokenExpiration(){
        return refreshExpirationMs;
    }

    public long getRemainingValidity(String token) {
        try {
            // parse jwt, xác thực chữ ký, lấy payload
            Date expiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            // lấy thời gian hết hạn - thời gian hiện tại
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e){
            // nếu token lỗi (expired, invalid, parse error) thì coi như hết hạn
            return 0;
        }
    }

    // sinh reset password token (thời hạn ngắn, VD 15 phút)
    public String generateResetPasswordToken(String subject){
        long resetExpirationMs = 15 * 60 * 1000; // 15 phút
        return Jwts.builder()
                .subject(subject) // email của user
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + resetExpirationMs))
                .claim("type", "reset") // đánh dấu token reset
                .signWith(key)
                .compact();
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
