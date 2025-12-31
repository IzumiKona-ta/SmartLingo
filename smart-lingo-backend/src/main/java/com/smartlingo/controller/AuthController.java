package com.smartlingo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlingo.entity.User;
import com.smartlingo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Map<String, Object> response = new HashMap<>();

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user != null && user.getPassword().equals(password)) {
            response.put("success", true);
            response.put("user", user);
            response.put("token", "mock-jwt-token-" + user.getId());
        } else {
            response.put("success", false);
            response.put("message", "Invalid username or password");
        }
        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Map<String, Object> response = new HashMap<>();

        User existing = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (existing != null) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return response;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setPoints(0);
        newUser.setStreakDays(0);
        newUser.setCurrentBook("CET4");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);

        userMapper.insert(newUser);

        response.put("success", true);
        response.put("user", newUser);
        return response;
    }
}
