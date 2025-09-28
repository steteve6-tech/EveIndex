-- Zeabur数据库初始化脚本
-- 这个脚本用于在Zeabur上初始化数据库和表结构

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE common_db;

-- 创建用户（如果不存在）
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'your-secure-password';
GRANT ALL PRIVILEGES ON common_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;

-- 显示数据库信息
SHOW DATABASES;
SELECT USER(), DATABASE();
