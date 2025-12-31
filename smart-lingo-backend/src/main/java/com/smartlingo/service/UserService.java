package com.smartlingo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlingo.entity.StudyLog;
import com.smartlingo.entity.User;
import com.smartlingo.mapper.StudyLogMapper;
import com.smartlingo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StudyLogMapper studyLogMapper;

    public Map<String, Object> getUserStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            // Create mock user if not exists for demo
            user = new User();
            user.setUsername("DemoUser");
            user.setPoints(1250);
            user.setStreakDays(12);
            user.setCreatedAt(LocalDateTime.now());
            // In real app, we would throw exception or return empty
        }

        // Get study logs for last 365 days
        QueryWrapper<StudyLog> query = new QueryWrapper<>();
        query.eq("user_id", userId)
             .ge("created_at", LocalDateTime.now().minusDays(365));
        List<StudyLog> logs = studyLogMapper.selectList(query);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("logs", logs);
        
        // Mock Radar Data
        Map<String, Integer> ability = new HashMap<>();
        ability.put("vocab", 80);
        ability.put("reading", 65);
        ability.put("listening", 70);
        ability.put("grammar", 60);
        result.put("ability", ability);

        return result;
    }

    public void checkIn(Long userId, String type) {
        // Update User
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + 10);
            // Simple streak logic (mock)
            user.setStreakDays(user.getStreakDays() + 1);
            userMapper.updateById(user);
        }

        // Add Log
        StudyLog log = new StudyLog();
        log.setUserId(userId);
        log.setActivityType(type);
        log.setPointsEarned(10);
        log.setCreatedAt(LocalDateTime.now());
        studyLogMapper.insert(log);
    }
}
