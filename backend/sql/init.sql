-- ============================================
-- 日常记账助手 - 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS expense_tracker DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

-- -------------------------------------------
-- 用户表：存储微信小程序用户信息
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    openid VARCHAR(64) NOT NULL COMMENT '微信小程序唯一标识',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    UNIQUE KEY uk_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------
-- 分类表：存储收入/支出分类
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属用户ID，0表示系统预置分类',
    name VARCHAR(20) NOT NULL COMMENT '分类名称',
    type TINYINT NOT NULL COMMENT '分类类型：0=支出，1=收入',
    icon VARCHAR(50) NOT NULL DEFAULT '' COMMENT '分类图标（emoji）',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统预置',
    is_hidden BOOLEAN DEFAULT FALSE COMMENT '是否隐藏（用户可隐藏分类）',
    sort_order INT DEFAULT 0 COMMENT '排序序号（越小越靠前）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_type (user_id, type) COMMENT '用户+类型联合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- -------------------------------------------
-- 账目记录表：存储每笔收支记录
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额：正数=收入，负数=支出',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    record_time DATETIME NOT NULL COMMENT '记账时间',
    remark VARCHAR(200) DEFAULT '' COMMENT '备注说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_user_time (user_id, record_time) COMMENT '用户+时间联合索引（用于按月查询）',
    INDEX idx_user_category (user_id, category_id) COMMENT '用户+分类联合索引（用于分类统计）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账目记录表';

-- -------------------------------------------
-- 预算表：存储用户月度预算
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    `year_month` VARCHAR(7) NOT NULL COMMENT '月份，格式：2026-06',
    amount DECIMAL(12,2) NOT NULL COMMENT '预算金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_month (user_id, `year_month`) COMMENT '用户+月份唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- -------------------------------------------
-- 预置分类数据
-- -------------------------------------------
-- 系统预置分类（user_id=0），包含支出和收入两大类
INSERT INTO categories (user_id, name, type, icon, is_system, sort_order) VALUES
-- 支出类（type=0）
(0, '餐饮', 0, '🍜', TRUE, 1),
(0, '交通', 0, '🚌', TRUE, 2),
(0, '购物', 0, '🛒', TRUE, 3),
(0, '娱乐', 0, '🎮', TRUE, 4),
(0, '居家', 0, '🏠', TRUE, 5),
(0, '医疗', 0, '💊', TRUE, 6),
(0, '教育', 0, '📚', TRUE, 7),
(0, '人情', 0, '🎁', TRUE, 8),
(0, '其他支出', 0, '📌', TRUE, 9),
-- 收入类（type=1）
(0, '工资', 1, '💰', TRUE, 1),
(0, '理财', 1, '📈', TRUE, 2),
(0, '其他收入', 1, '💵', TRUE, 3);
