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


}
