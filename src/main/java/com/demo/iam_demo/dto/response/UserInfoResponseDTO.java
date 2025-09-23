package com.demo.iam_demo.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String avatar;
    private boolean active;
    private Set<String> roles;
}
