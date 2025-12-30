package com.smartlingo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlingo.entity.StudyLog;
import com.smartlingo.entity.User;
import com.smartlingo.mapper.StudyLogMapper;
import com.smartlingo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StudyLogMapper studyLogMapper;

    @GetMapping("/stats")
    public Map<String, Object> getUserStats(@RequestParam Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            // Create default user if not exists for demo
            user = new User();
            user.setId(userId);
            user.setUsername("Demo User");
            user.setPoints(0);
            user.setStreakDays(0);
            user.setCurrentBook("CET4");
            // In real app, we would insert this user
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        
        // 1. Calculate Real Stats
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        
        // Today Learned (Count VOCAB logs created today)
        Long todayLearned = studyLogMapper.selectCount(new QueryWrapper<StudyLog>()
                .eq("user_id", userId)
                .eq("activity_type", "VOCAB")
                .eq("book_type", user.getCurrentBook())
                .ge("created_at", todayStart));
                
        // Total Learned (Count all VOCAB logs)
        Long totalLearned = studyLogMapper.selectCount(new QueryWrapper<StudyLog>()
                .eq("user_id", userId)
                .eq("activity_type", "VOCAB")
                .eq("book_type", user.getCurrentBook()));
                
        // Today Duration (Mock for now as we don't track duration yet, or use count * 0.5 mins)
        long todayDuration = todayLearned * 1; // 1 min per word estimate
        
        // Total Duration
        long totalDuration = totalLearned * 1;

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayLearned", todayLearned);
        stats.put("totalLearned", totalLearned);
        stats.put("todayDuration", todayDuration);
        stats.put("totalDuration", totalDuration);
        
        // Extended stats for Stats Page
        stats.put("masteryCount", totalLearned); // Using total learned as mastery for MVP
        stats.put("streakDays", user.getStreakDays());
        
        // Calculate growth (Real Logic)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime fourteenDaysAgo = now.minusDays(14);

        Long last7DaysCount = studyLogMapper.selectCount(new QueryWrapper<StudyLog>()
                .eq("user_id", userId)
                .ge("created_at", sevenDaysAgo));

        Long prev7DaysCount = studyLogMapper.selectCount(new QueryWrapper<StudyLog>()
                .eq("user_id", userId)
                .ge("created_at", fourteenDaysAgo)
                .lt("created_at", sevenDaysAgo));
        
        long growth = 0;
        if (prev7DaysCount > 0) {
            growth = ((last7DaysCount - prev7DaysCount) * 100) / prev7DaysCount;
        } else if (last7DaysCount > 0) {
            growth = 100;
        }

        stats.put("durationGrowth", growth); 
        stats.put("masteryGrowth", last7DaysCount); // Show actual count increase, not percentage for mastery? UI says "+56", implies count.
        // Actually UI says "+12%" for duration and "+56" for mastery.
        // So durationGrowth is percentage, masteryGrowth is absolute count.
        
        response.put("stats", stats);

        // 2. Generate Calendar Data (Last 7 days)
        List<Map<String, Object>> calendarDays = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // We want to show a week view, maybe ending today or centering today? 
        // Dashboard typically shows "This week" or "Last 7 days". 
        // Let's show last 6 days + today.
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            
            // Check if user has any log on this day
            Long count = studyLogMapper.selectCount(new QueryWrapper<StudyLog>()
                    .eq("user_id", userId)
                    .ge("created_at", dayStart)
                    .le("created_at", dayEnd));
            
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("week", date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            dayInfo.put("date", date.getDayOfMonth());
            dayInfo.put("count", count);
            dayInfo.put("checked", count > 0);
            dayInfo.put("isToday", i == 0);
            calendarDays.add(dayInfo);
        }
        response.put("calendarDays", calendarDays);

        // Mock ability data (Radar chart) - Keep as mock for now unless we have sophisticated scoring
        Map<String, Integer> ability = new HashMap<>();
        ability.put("vocab", Math.min(100, (int)(totalLearned / 10))); // Simple progression
        ability.put("reading", 70);
        ability.put("listening", 60);
        ability.put("grammar", 75);
        response.put("ability", ability);
        
        return response;
    }

    @PostMapping("/signin")
    public Map<String, Object> signIn(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        Map<String, Object> response = new HashMap<>();
        
        // 1. Check if signed in today
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todaySignIn = studyLogMapper.selectCount(new QueryWrapper<StudyLog>()
                .eq("user_id", userId)
                .eq("activity_type", "SIGN_IN")
                .ge("created_at", todayStart));
        
        User user = userMapper.selectById(userId);
        
        if (todaySignIn > 0) {
            response.put("success", false);
            response.put("message", "Already signed in today");
            response.put("streakDays", user.getStreakDays());
            return response;
        }
        
        // 2. Update Streak
        // Check yesterday's activity to maintain streak
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayStart.minusSeconds(1);
        
        Long yesterdayActivity = studyLogMapper.selectCount(new QueryWrapper<StudyLog>()
                .eq("user_id", userId)
                .ge("created_at", yesterdayStart)
                .le("created_at", yesterdayEnd));
        
        if (yesterdayActivity > 0) {
            user.setStreakDays((user.getStreakDays() == null ? 0 : user.getStreakDays()) + 1);
        } else {
            user.setStreakDays(1);
        }
        userMapper.updateById(user);
        
        // 3. Create Log
        StudyLog log = new StudyLog();
        log.setUserId(userId);
        log.setActivityType("SIGN_IN");
        log.setCreatedAt(LocalDateTime.now());
        studyLogMapper.insert(log);
        
        response.put("success", true);
        response.put("streakDays", user.getStreakDays());
        
        return response;
    }

    @PostMapping("/book")
    public Map<String, Object> updateBook(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        String book = (String) body.get("book");
        
        User user = new User();
        user.setId(userId);
        user.setCurrentBook(book);
        
        userMapper.updateById(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("currentBook", book);
        return response;
    }

    @PostMapping("/reset-book")
    public Map<String, Object> resetBook(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        String book = (String) body.get("book");
        
        // Delete all VOCAB logs for this user and book
        studyLogMapper.delete(new QueryWrapper<StudyLog>()
                .eq("user_id", userId)
                .eq("activity_type", "VOCAB")
                .eq("book_type", book));
                
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    @PostMapping("/create-test-user")
    public Map<String, Object> createTestUser() {
        // Create or Reset User ID 999
        Long userId = 999L;
        User user = userMapper.selectById(userId);
        if (user == null) {
            user = new User();
            user.setId(userId);
            user.setUsername("TestUser");
            user.setPoints(0);
            user.setStreakDays(0);
            user.setCurrentBook("CET4");
            userMapper.insert(user); // Assuming non-auto-increment or handled
        } else {
             // Reset logs
             studyLogMapper.delete(new QueryWrapper<StudyLog>().eq("user_id", userId));
        }

        // Generate logs for last 5 days
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            int wordsCount = 100 + random.nextInt(50); // 100-150 words
            LocalDateTime date = now.minusDays(i).withHour(10);
            
            for (int j = 0; j < wordsCount; j++) {
                StudyLog log = new StudyLog();
                log.setUserId(userId);
                log.setActivityType("VOCAB");
                log.setBookType("CET4");
                log.setCreatedAt(date.plusMinutes(j % 60));
                log.setWordId((long)j);
                studyLogMapper.insert(log);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Test user 999 created with history");
        response.put("userId", userId);
        return response;
    }
}
