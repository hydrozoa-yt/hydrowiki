CREATE DATABASE IF NOT EXISTS `hydrowiki`;

USE `hydrowiki`;

CREATE USER 'hydrowiki_user'@'%' IDENTIFIED BY 'password';

GRANT ALL PRIVILEGES ON `hydrowiki`.* TO 'hydrowiki_user'@'%';

FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `articles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `article_edits` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `article_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `unified_diff_to_prev` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO `users` (`username`, `email`, `password`) VALUES
('admin_user', 'admin@example.com', 'password'),
('test_user', 'test@example.com', 'password');

INSERT INTO `articles` (`title`, `content`) VALUES
('Invasion_of_the_heymen', 'This is an article describing the historical event "Invasion of the heymen", and it explains the happenings as well as the historical significance of the event.'),
('Lesson_in_never_backing_down', 'This is a description of the steps one needs to take, in order to never back down.');