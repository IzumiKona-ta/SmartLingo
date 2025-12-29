package com.smartlingo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        initializeUserWordsTable();
        initializeUsersTable();
        initializeStudyLogsTable();
    }

    private void initializeUserWordsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_words (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "word VARCHAR(255) NOT NULL, " +
                "translate TEXT, " +
                "phonetic VARCHAR(255), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_user_word (user_id, word)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        
        try {
            jdbcTemplate.execute(sql);
            System.out.println("Database table 'user_words' checked/created successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database table 'user_words': " + e.getMessage());
        }
    }

    private void initializeUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL UNIQUE, " +
                "password VARCHAR(255), " +
                "avatar VARCHAR(255), " +
                "points INT DEFAULT 0, " +
                "streak_days INT DEFAULT 0, " +
                "current_book VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try {
            jdbcTemplate.execute(sql);
            System.out.println("Database table 'users' checked/created successfully.");
            
            // Ensure default user exists
            String checkUserSql = "SELECT count(*) FROM users WHERE id = 1";
            Integer count = jdbcTemplate.queryForObject(checkUserSql, Integer.class);
            if (count != null && count == 0) {
                String insertUserSql = "INSERT INTO users (id, username, password, points, streak_days, current_book) VALUES (1, 'User', '123456', 0, 0, 'CET4')";
                jdbcTemplate.execute(insertUserSql);
                System.out.println("Default user (ID 1) created.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize database table 'users': " + e.getMessage());
        }
    }

    private void initializeStudyLogsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS study_logs (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "activity_type VARCHAR(50), " +
                "points_earned INT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try {
            jdbcTemplate.execute(sql);
            System.out.println("Database table 'study_logs' checked/created successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database table 'study_logs': " + e.getMessage());
        }
    }
}
