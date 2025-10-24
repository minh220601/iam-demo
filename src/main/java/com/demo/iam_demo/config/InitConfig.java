package com.demo.iam_demo.config;

import com.demo.iam_demo.model.Role;
import com.demo.iam_demo.model.User;
import com.demo.iam_demo.repository.RoleRepository;
import com.demo.iam_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class InitConfig {
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private static final String ADMIN_EMAIL = "admin123@gmail.com";
    private static final String ADMIN_PASSWORD = "123456";
    private static final String ADMIN_USERNAME = "admin";

    @Bean
    ApplicationRunner initData(){
        return args -> {
            log.info("Initializing default roles and admin user.");

            //tạo role nếu chưa có
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
            Role modRole = roleRepository.findByName("ROLE_MODERATOR")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_MODERATOR").build()));

            // tạo admin user nếu chưa có
            if(userRepository.findByEmail(ADMIN_EMAIL).isEmpty()){
                User admin = User.builder()
                        .email(ADMIN_EMAIL)
                        .username(ADMIN_USERNAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .active(true)
                        .build();

                // gán quyền admin
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
                log.warn("Admin user created: {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
                log.info("Please change the password after first login");
            }
            log.info("Initialization complete");
        };
    }
}