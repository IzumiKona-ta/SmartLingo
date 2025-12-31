package com.smartlingo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlingo.entity.UserWord;
import com.smartlingo.mapper.UserWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notebook")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class UserWordController {

    @Autowired
    private UserWordMapper userWordMapper;

    @GetMapping
    public Map<String, Object> getUserWords(@RequestParam Long userId) {
        System.out.println("getUserWords called for userId: " + userId);
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<UserWord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).orderByDesc("created_at");
            List<UserWord> words = userWordMapper.selectList(queryWrapper);
            System.out.println("Fetched words count: " + (words != null ? words.size() : "null"));
            response.put("words", words);
            response.put("success", true);
        } catch (Exception e) {
            System.err.println("Error in getUserWords: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/add")
    public Map<String, Object> addWord(@RequestBody UserWord userWord) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check if already exists
            QueryWrapper<UserWord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userWord.getUserId())
                        .eq("word", userWord.getWord());
            
            if (userWordMapper.selectCount(queryWrapper) > 0) {
                response.put("success", false);
                response.put("message", "单词已在生词本中");
                return response;
            }

            userWord.setCreatedAt(LocalDateTime.now());
            userWordMapper.insert(userWord);
            response.put("success", true);
            response.put("id", userWord.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> removeWord(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userWordMapper.deleteById(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/check")
    public Map<String, Object> checkWord(@RequestParam Long userId, @RequestParam String word) {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<UserWord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("word", word);
            boolean exists = userWordMapper.selectCount(queryWrapper) > 0;
            response.put("exists", exists);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
