package com.demo.iam_demo.jwt;

import com.demo.iam_demo.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException{
        try{
            //Lấy JWT từ request
            String jwt = parseJwt(request); //lấy JWT từ header Authorization bằng cách bỏ tiền tố Bearer

            //Kiểm tra token hợp lệ
            if(jwt != null && jwtUtils.validateJwtToken(jwt)){ //check token có đúng chữ ký hay hết hạn
                String username = jwtUtils.getUserNameFromJwtToken(jwt); //lấy username từ JWT

                UserDetails userDetails = userDetailsService.loadUserByUsername(username); //load UserDetail từ DB
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken( //tạo Authentication object
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                //Gắn thông tin vào SecurityContext
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e){
            logger.error("Cannot set user authentication: ", e);
        }
        filterChain.doFilter(request, response); //phải gọi filterChain để request đi tiếp sang filter tiếp theo hoặc controller, nếu không gọi thì request bị chặn ở đây
    }

    private String parseJwt(HttpServletRequest request){
        String headerAuth = request.getHeader("Authorization");

        if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }

        return null;
    }
}
