package com.demo.iam_demo.controller;

import com.demo.iam_demo.dto.request.ChangePasswordRequest;
import com.demo.iam_demo.dto.request.UpdateProfileRequest;
import com.demo.iam_demo.dto.request.UserInfoRequest;
import com.demo.iam_demo.dto.request.UserRequest;
import com.demo.iam_demo.dto.response.UserInfoResponseDTO;
import com.demo.iam_demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // tạo mới user
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponseDTO> createUser(@RequestBody @Valid UserRequest request){
        return ResponseEntity.ok(userService.createUser(request));
    }

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
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.ok("Delete user successfully");
    }

    // đổi password
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<String> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request){
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok("Password change successfully");
    }

    // upload avatar
    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal){
        String imageUrl = userService.updateAvatar(principal, file);
        return ResponseEntity.ok(imageUrl);
    }
}
