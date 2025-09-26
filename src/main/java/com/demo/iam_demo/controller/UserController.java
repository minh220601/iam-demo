package com.demo.iam_demo.controller;

import com.demo.iam_demo.dto.request.UpdateProfileRequest;
import com.demo.iam_demo.dto.request.UserInfoRequest;
import com.demo.iam_demo.dto.response.UserInfoResponseDTO;
import com.demo.iam_demo.model.User;
import com.demo.iam_demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // lấy danh sách user (admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfoResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // xem info 1 user theo id (admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponseDTO> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // xem info của user đang đăng nhập
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserInfoResponseDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserByEmail(authentication.getName()));
    }

    // update info của user đang đăng nhập
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserInfoResponseDTO> updateUserProfile(Authentication authentication, @RequestBody UpdateProfileRequest request){
        return ResponseEntity.ok(userService.updateUserByEmail(authentication.getName(), request));
    }

    // update info user khác (admin)
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponseDTO> updateUserByAdmin(@PathVariable Long id, @RequestBody UserInfoRequest request){
        return ResponseEntity.ok(userService.updateUserByAdmin(id, request));
    }

    // xóa user (admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
