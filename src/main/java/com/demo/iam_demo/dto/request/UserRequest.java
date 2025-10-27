package com.demo.iam_demo.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserRequest {
    private String username;
    private String email;
    private String password;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String avatar;
    private boolean active;
    private Set<String> roles;
}
