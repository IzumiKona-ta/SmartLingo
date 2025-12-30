-- 1. 尝试添加 book_type 列 (如果已存在则此句会报错，可忽略)
-- 注意：如果 study_logs 表中已经存在 book_type 列，您可以注释掉下面这行
-- ALTER TABLE study_logs ADD COLUMN book_type VARCHAR(50) DEFAULT 'CET4';

-- 由于您之前报告 book_type 缺失，而 word_id 已存在，我们只添加 book_type
-- 使用存储过程来安全地添加列 (避免报错停止)
DROP PROCEDURE IF EXISTS AddBookTypeColumn;

DELIMITER //
CREATE PROCEDURE AddBookTypeColumn()
BEGIN
    IF NOT EXISTS (
        SELECT * 
        FROM information_schema.columns 
        WHERE table_name = 'study_logs' AND column_name = 'book_type'
    ) THEN
        ALTER TABLE study_logs ADD COLUMN book_type VARCHAR(50) DEFAULT 'CET4';
    END IF;
END //
DELIMITER ;

CALL AddBookTypeColumn();
DROP PROCEDURE AddBookTypeColumn;


-- 2. 重置并创建测试用户 (ID: 999)
DELETE FROM study_logs WHERE user_id = 999;
DELETE FROM users WHERE id = 999;

INSERT INTO users (id, username, password, points, streak_days, current_book, created_at) 
VALUES (999, 'TestUser', '123456', 1000, 5, 'CET4', NOW());

-- 3. 插入学习记录 (过去 3 天)
-- Day 0 (今天) - 20 词
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 1, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 2, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 3, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 4, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 5, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 6, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 7, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 8, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 9, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 10, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 11, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 12, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 13, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 14, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 15, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 16, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 17, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 18, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 19, 'VOCAB', 'CET4', 10, NOW());
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 20, 'VOCAB', 'CET4', 10, NOW());

-- Day -1 (昨天) - 10 词
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 21, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 22, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 23, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 24, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 25, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 26, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 27, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 28, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 29, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 30, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Day -2 (前天) - 5 词
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 41, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 2 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 42, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 2 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 43, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 2 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 44, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 2 DAY));
INSERT INTO study_logs (user_id, word_id, activity_type, book_type, points_earned, created_at) VALUES (999, 45, 'VOCAB', 'CET4', 10, DATE_SUB(NOW(), INTERVAL 2 DAY));
