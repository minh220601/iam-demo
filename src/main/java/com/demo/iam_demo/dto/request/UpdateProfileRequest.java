package com.demo.iam_demo.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    // dùng cho user chỉnh sửa info
    private String username;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String avatar;
}
