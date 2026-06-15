package com.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 日常记账助手 - 后端服务启动类
 * 
 * 功能说明：
 * - 提供 RESTful API 接口，支持微信小程序的记账功能
 * - 包含用户认证、记录管理、分类管理、统计报表等模块
 */
@SpringBootApplication
public class ExpenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseApplication.class, args);
    }
}
