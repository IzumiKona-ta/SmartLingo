package com.smartlingo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlingo.entity.BaseWord;
import com.smartlingo.entity.Cet4Word;
import com.smartlingo.entity.Cet6Word;
import com.smartlingo.entity.GraduateWord;
import com.smartlingo.mapper.Cet4Mapper;
import com.smartlingo.mapper.Cet6Mapper;
import com.smartlingo.mapper.GraduateMapper;
import com.smartlingo.entity.StudyLog;
import com.smartlingo.mapper.StudyLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/words")
public class WordController {

    @Autowired
    private Cet4Mapper cet4Mapper;

    @Autowired
    private Cet6Mapper cet6Mapper;

    @Autowired
    private GraduateMapper graduateMapper;

    @Autowired
    private StudyLogMapper studyLogMapper;

    @Autowired
    private com.smartlingo.mapper.UserMapper userMapper;

    @GetMapping("/{type}")
    public Object getWords(@PathVariable String type, @RequestParam(defaultValue = "20") int limit, @RequestParam(required = false) Long userId) {
        try {
            // Handle "CURRENT" type which means fetch based on user's selection
            if ("CURRENT".equalsIgnoreCase(type)) {
                 if (userId == null) {
                    userId = 1L; // Fallback only if not provided
                 }
                 com.smartlingo.entity.User user = userMapper.selectById(userId);
                 String book = (user != null && user.getCurrentBook() != null) ? user.getCurrentBook() : "CET4";
                 
                 // Fetch learned word IDs to exclude
                 List<StudyLog> allLogs = studyLogMapper.selectList(new QueryWrapper<StudyLog>().eq("user_id", userId));
                System.out.println("DEBUG: Total logs for user " + userId + ": " + allLogs.size());
                for (StudyLog log : allLogs) {
                    System.out.println("DEBUG Log: id=" + log.getId() + ", wordId=" + log.getWordId() + ", type=" + log.getActivityType());
                }

                List<StudyLog> logs = studyLogMapper.selectList(new QueryWrapper<StudyLog>()
                        .eq("user_id", userId)
                        .eq("activity_type", "VOCAB")
                        .isNotNull("word_id"));
                 
                 List<Long> learnedIds = logs.stream()
                        .map(StudyLog::getWordId)
                        .distinct()
                        .collect(Collectors.toList());
                 
                 // Debugging
                 System.out.println("Fetching words for user " + userId + ". Learned count: " + learnedIds.size());
                 if (!learnedIds.isEmpty()) {
                     System.out.println("Learned IDs: " + learnedIds);
                 }
                 
                 // Construct query: WHERE id NOT IN (...) ORDER BY id ASC LIMIT limit
                 QueryWrapper wrapper = new QueryWrapper<>();
                 if (!learnedIds.isEmpty()) {
                     wrapper.notIn("id", learnedIds);
                 }
                 wrapper.last("ORDER BY id ASC LIMIT " + limit);
                 
                 List<?> words = Collections.emptyList();
                 if ("CET4".equalsIgnoreCase(book)) {
                     words = cet4Mapper.selectList(wrapper);
                 } else if ("CET6".equalsIgnoreCase(book)) {
                     words = cet6Mapper.selectList(wrapper);
                 } else if ("KAOYAN".equalsIgnoreCase(book) || "GRADUATE".equalsIgnoreCase(book)) {
                     words = graduateMapper.selectList(wrapper);
                 }
                 
                 System.out.println("Returned words count: " + words.size());
                 if (!words.isEmpty()) {
                     System.out.println("First word sample: " + words.get(0));
                 }
                 return words;
            }
    
            // Use ORDER BY RAND() to fetch random words
            String randomSql = "ORDER BY RAND() LIMIT " + limit;
            
            Object result = Collections.emptyList();
            if ("CET4".equalsIgnoreCase(type)) {
                result = cet4Mapper.selectList(new QueryWrapper<Cet4Word>().last(randomSql));
            } else if ("CET6".equalsIgnoreCase(type)) {
                result = cet6Mapper.selectList(new QueryWrapper<Cet6Word>().last(randomSql));
            } else if ("KAOYAN".equalsIgnoreCase(type) || "GRADUATE".equalsIgnoreCase(type)) {
                result = graduateMapper.selectList(new QueryWrapper<GraduateWord>().last(randomSql));
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            // Return error object
            return java.util.Map.of("error", "Failed to load words: " + e.getMessage());
        }
    }
}
