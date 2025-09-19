package com.demo.iam_demo.jwt;

import com.demo.iam_demo.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.Secret}")
    private String jwtSecret;

    //thời gian token sống (giây)
    @Value("${jwt.valid-duration}")
    private long jwtValidDuration;

    //thời gian token có thể refresh (giây)
    @Value("${jwt.refreshable-duration}")
    private long jwtRefreshableDuration;

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //sinh JWT khi user login thành công
    public String generateJwtToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtValidDuration * 1000);//đổi second -> milisecond

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    //lấy username từ JWT
    public String getUserNameFromJwtToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey()
                .build()
                .parseClaimJws(token)
                .getBody()
                .getSubject();
    }

    //kiểm tra token hợp lệ
    public boolean validateJwtToken(String authToken){
        try{
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e){
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e){
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e){
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e){
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    //kiểm tra token còn trong thời gian refresh không
    public boolean isTokenRefreshable(String token){
        try {
            Date issuedAt = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parserClaimsJws(token)
                    .getBody()
                    .getIssuedAt();

            long now = System.currentTimeMillis();
            return (now - issuedAt.getTime()) < jwtRefreshableDuration * 1000;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
