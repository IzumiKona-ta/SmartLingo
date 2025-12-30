package com.smartlingo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlingo.entity.StudyLog;
import com.smartlingo.mapper.StudyLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class StudyController {

    @Autowired
    private StudyLogMapper studyLogMapper;

    @PostMapping("/checkin")
    public Map<String, Object> checkin(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            String type = (String) payload.getOrDefault("type", "VOCAB"); // VOCAB, READING, etc.
            
            StudyLog log = new StudyLog();
            log.setUserId(userId);
            if (payload.containsKey("wordId") && payload.get("wordId") != null) {
                log.setWordId(Long.valueOf(payload.get("wordId").toString()));
                System.out.println("Checkin for wordId: " + log.getWordId());
            } else {
                System.out.println("Checkin missing wordId!");
            }
            log.setActivityType(type);
            log.setCreatedAt(LocalDateTime.now());
            // In a real app, we might track duration or specific word IDs here
            
            studyLogMapper.insert(log);
            
            response.put("success", true);
            response.put("message", "Checkin successful");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
