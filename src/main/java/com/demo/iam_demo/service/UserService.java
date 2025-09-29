package com.demo.iam_demo.service;

import com.demo.iam_demo.dto.request.ChangePasswordRequest;
import com.demo.iam_demo.dto.request.UpdateProfileRequest;
import com.demo.iam_demo.dto.request.UserInfoRequest;
import com.demo.iam_demo.dto.response.UserInfoResponseDTO;
import com.demo.iam_demo.mapper.UserMapper;
import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.model.User;
import com.demo.iam_demo.repository.RoleRepository;
import com.demo.iam_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // lấy danh sách tất cả user
    public List<UserInfoResponseDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(userMapper::userInfoResponseDto)
                .toList();
    }

    public UserInfoResponseDTO getUserById(Long id){
        return userMapper.userInfoResponseDto(
                userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"))
        );
    }

    public UserInfoResponseDTO getUserByEmail(String email){
        return userMapper.userInfoResponseDto(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + email))
        );
    }

    // cập nhật user info với role user
    public UserInfoResponseDTO updateUserByEmail(String email, UpdateProfileRequest updateUser){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(updateUser.getUsername());
        user.setAddress(updateUser.getAddress());
        user.setPhone(updateUser.getPhone());
        user.setAvatar(updateUser.getAvatar());
        user.setBirthDate(updateUser.getBirthDate());
        return userMapper.userInfoResponseDto(userRepository.save(user));
    }

    // cập nhật user info với role admin
    public  UserInfoResponseDTO updateUserByAdmin(Long id, UserInfoRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());

        // cập nhật trạng thái active
        user.setActive(request.isActive());

        // cập nhật role
        if (request.getRoles() != null && !request.getRoles().isEmpty()){
            Set<Role> newRole = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(newRole);
        }

        return userMapper.userInfoResponseDto(userRepository.save(user));
    }

    // xóa user
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    // đổi mật khẩu
    public void changePassword(String email, ChangePasswordRequest request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new RuntimeException("Old password is incorrect");
        }

        // mã hóa mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
