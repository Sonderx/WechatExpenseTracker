package com.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.expense.dto.BudgetSetRequest;
import com.expense.entity.Budget;
import com.expense.mapper.BudgetMapper;
import com.expense.mapper.RecordMapper;
import com.expense.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 预算服务实现类
 * 
 * 功能说明：
 * - 实现预算的设置和查询
 * - 计算预算使用进度
 * - 每个用户每月只能有一个预算
 */
@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private RecordMapper recordMapper;

    /**
     * 设置月度预算
     * 如果该月已有预算则更新，否则新建
     */
    @Override
    public void setBudget(Long userId, BudgetSetRequest request) {
        // 查询是否已有该月预算
        Budget existing = budgetMapper.selectOne(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getYearMonth, request.getYearMonth())
        );

        if (existing != null) {
            // 已有预算，更新金额
            existing.setAmount(request.getAmount());
            budgetMapper.updateById(existing);
        } else {
            // 没有预算，新建
            Budget budget = new Budget();
            budget.setUserId(userId);
            budget.setYearMonth(request.getYearMonth());
            budget.setAmount(request.getAmount());
            budgetMapper.insert(budget);
        }
    }

    /**
     * 获取指定月份的预算
     */
    @Override
    public Budget getBudget(Long userId, String yearMonth) {
        return budgetMapper.selectOne(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getYearMonth, yearMonth)
        );
    }

    /**
     * 计算预算使用进度
     * 返回 已支出/预算总额 的百分比（0.00 ~ 1.00）
     * 如果没有设置预算，返回 null
     */
    @Override
    public BigDecimal getBudgetProgress(Long userId, String yearMonth) {
        Budget budget = getBudget(userId, yearMonth);
        if (budget == null || budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // 获取当月支出总额（支出存为负数，取绝对值）
        BigDecimal totalExpense = recordMapper.sumExpenseByMonth(userId, yearMonth);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }
        // 支出总额取绝对值
        totalExpense = totalExpense.abs();

        // 计算使用进度（保留两位小数）
        return totalExpense.divide(budget.getAmount(), 4, RoundingMode.HALF_UP);
    }
}
