package com.smartlingo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlingo.entity.BaseWord;
import com.smartlingo.entity.Cet4Word;
import com.smartlingo.entity.Cet6Word;
import com.smartlingo.entity.GraduateWord;
import com.smartlingo.mapper.Cet4Mapper;
import com.smartlingo.mapper.Cet6Mapper;
import com.smartlingo.mapper.GraduateMapper;
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
    private com.smartlingo.mapper.UserMapper userMapper;

    @GetMapping("/{type}")
    public Object getWords(@PathVariable String type, @RequestParam(defaultValue = "20") int limit) {
        try {
            // Handle "CURRENT" type which means fetch based on user's selection
            if ("CURRENT".equalsIgnoreCase(type)) {
                 // For demo, we hardcode userId 1. In real app, get from session/token
                 com.smartlingo.entity.User user = userMapper.selectById(1L);
                 String book = (user != null && user.getCurrentBook() != null) ? user.getCurrentBook() : "CET4";
                 return getWords(book, limit);
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
