package com.expense.service;

import com.expense.dto.BudgetSetRequest;
import com.expense.entity.Budget;

import java.math.BigDecimal;

/**
 * 预算服务接口
 * 
 * 功能说明：
 * - 定义预算相关的业务操作
 * - 支持设置、查询月度预算
 * - 支持计算预算剩余和超支情况
 */
public interface BudgetService {

    /**
     * 设置月度预算（如果已存在则更新）
     * 
     * @param userId 用户ID
     * @param request 预算设置请求
     */
    void setBudget(Long userId, BudgetSetRequest request);

    /**
     * 获取指定月份的预算
     * 
     * @param userId 用户ID
     * @param yearMonth 月份，格式：2026-06
     * @return 预算对象，不存在返回 null
     */
    Budget getBudget(Long userId, String yearMonth);

    /**
     * 计算预算使用情况
     * 
     * @param userId 用户ID
     * @param yearMonth 月份
     * @return 使用进度（已支出/预算总额）
     */
    BigDecimal getBudgetProgress(Long userId, String yearMonth);
}
