package com.demo.iam_demo.service;

import com.demo.iam_demo.dto.request.ChangePasswordRequest;
import com.demo.iam_demo.dto.request.UpdateProfileRequest;
import com.demo.iam_demo.dto.request.UserInfoRequest;
import com.demo.iam_demo.dto.request.UserRequest;
import com.demo.iam_demo.dto.response.UserInfoResponseDTO;
import com.demo.iam_demo.mapper.UserMapper;
import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.model.User;
import com.demo.iam_demo.repository.RoleRepository;
import com.demo.iam_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
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
    private final CloudinaryService cloudinaryService;
    private final UserActivityLogService userActivityLogService;

    // tạo user (admin)
    public UserInfoResponseDTO createUser(UserRequest request){
        // kiểm tra email đã tồn tại
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists");
        }

        // mã hóa password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .birthDate(request.getBirthDate())
                .address(request.getAddress())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .active(request.isActive())
                .build();

        // gán role
        if (request.getRoles() != null && !request.getRoles().isEmpty()){
            Set<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            // nếu không gửi role thì mặc định là ROLE_USER
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.getRoles().add(defaultRole);
        }

        // lưu user mới vào database
        User savedUser = userRepository.save(user);

        // trả về DTO
        return userMapper.userInfoResponseDto(savedUser);
    }

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

        // ghi log đổi password
        userActivityLogService.log(user.getId(), "CHANGE_PASSWORD");
    }

    // cập nhật ảnh profile
    public String updateAvatar(Principal principal, MultipartFile file){
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // upload ảnh lên cloudinary
        String imageUrl = cloudinaryService.uploadImage(file, "profile_pictures");

        // cập nhật url vào user
        user.setAvatar(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }
}
