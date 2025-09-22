package com.demo.iam_demo.config;

import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader {
    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles(){
        if (roleRepository.findByName("ROLE_USER").isEmpty()){
            roleRepository.save(Role.builder().name("ROLE_USER").build());
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()){
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        }
        if (roleRepository.findByName("ROLE_MODERATOR").isEmpty()){
            roleRepository.save(Role.builder().name("ROLE_MODERATOR").build());
        }
    }
}
