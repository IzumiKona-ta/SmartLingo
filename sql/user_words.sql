CREATE TABLE IF NOT EXISTS `user_words` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `word` varchar(255) NOT NULL,
  `translate` varchar(500) DEFAULT NULL,
  `phonetic` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
