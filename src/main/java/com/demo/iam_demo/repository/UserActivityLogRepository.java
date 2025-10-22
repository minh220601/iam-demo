package com.demo.iam_demo.repository;

import com.demo.iam_demo.model.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
}
