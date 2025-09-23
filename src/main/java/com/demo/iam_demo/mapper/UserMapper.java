package com.demo.iam_demo.mapper;

import com.demo.iam_demo.dto.response.UserInfoResponseDTO;
import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserInfoResponseDTO userInfoResponseDto(User user);

    // helper để lấy role name thay vì entity
    default Set<String> mapRoles(Set<Role> roles){
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
