package com.demo.iam_demo.security;

import com.demo.iam_demo.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException{

        //lấy Authorization header
        final String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // không có token thì bỏ qua

        }

        final String token = authHeader.substring(7); // cắt Bearer

        // kiểm tra token có bị blacklist không
        String isBlacklisted = redisTemplate.opsForValue().get("blacklist_token:" + token);
        if(isBlacklisted != null){
            //filterChain.doFilter(request, response); // bỏ qua, coi như không auth
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been revoked (blacklist)");
            return;
        }

        final String userEmail = jwtUtils.extractSubject(token);

        // nếu có email và SecurityContext chưa set Authentication
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){

            // load user từ DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // validate token
            if(jwtUtils.validateToken(token)){
                // kiểm tra loại token
                String type = jwtUtils.extractTokenType(token);
                if (!"access".equals(type)){
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid token type: " + type);
                    return;
                }

                //nếu hợp lệ -> set authentication
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // set vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}
