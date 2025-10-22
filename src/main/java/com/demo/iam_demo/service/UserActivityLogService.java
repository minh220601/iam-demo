package com.demo.iam_demo.service;

import com.demo.iam_demo.model.UserActivityLog;
import com.demo.iam_demo.repository.UserActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserActivityLogService {
    private final UserActivityLogRepository logRepository;

    public void log(Long userId, String activity){
        UserActivityLog log = UserActivityLog.builder()
                .userId(userId)
                .activity(activity)
                .createdAt(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }
}
