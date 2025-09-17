package com.demo.iam_demo.repository;

import com.demo.iam_demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
