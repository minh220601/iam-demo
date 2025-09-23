package com.demo.iam_demo.service;

import com.demo.iam_demo.model.User;
import com.demo.iam_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // lấy danh sách tất cả user
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // cập nhật user info
    public User updateUser(Long id, User updateUser){
        User user = getUserById(id);
        user.setUsername(updateUser.getUsername());
        user.setAddress(updateUser.getAddress());
        user.setPhone(updateUser.getPhone());
        user.setAvatar(updateUser.getAvatar());
        user.setBirthDate(updateUser.getBirthDate());
        return userRepository.save(user);
    }

    // xóa user
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }
}
