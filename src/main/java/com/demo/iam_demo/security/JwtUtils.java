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
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-seconds}") long accessTokenExpiration,
            @Value("${jwt.refresh-exp-seconds}") long refreshTokenExpiration
    ){
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); //Tạo key từ secret
        this.accessTokenExpiration = accessTokenExpiration * 1000; // đổi second -> ms
        this.refreshTokenExpiration = refreshTokenExpiration * 1000;
    }

    //sinh access token
    public String generateAccessToken(String subject){
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    //sinh refresh token
    public String generateRefreshToken(String subject){
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
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
        } catch (Exception e){
            return false;
        }
    }

    public long getRefreshTokenExpiration(){
        return refreshTokenExpiration;
    }

    public long getRemainingValidity(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
