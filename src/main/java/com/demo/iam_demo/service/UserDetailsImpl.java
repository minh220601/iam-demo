package com.demo.iam_demo.service;

import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;

    @JsonIgnore
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    //tạo UserDetailsImpl từ entity User
    public static UserDetailsImpl build(User user){
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName) // Role.name
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .collect(Collectors.toSet());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

//    private final User user;
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities(){
//        Set<Role> roles = user.getRoles();
//        return roles.stream()
//                .map(role -> new SimpleGrantedAuthority(role.getName()))
//                .collect(Collectors.toSet());
//    }

    //Spring Security dùng username để login
    @Override
    public String getUsername(){
        return email; // dùng email làm username
    }

    @Override
    public String getPassword(){
        return password;
    }

    //tạm thời để toàn bộ return true -> account luôn active, không bị khóa, không hết hạn
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    public Long getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }
}
