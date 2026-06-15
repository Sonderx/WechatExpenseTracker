package com.expense.controller;

import com.expense.common.Result;
import com.expense.dto.BudgetSetRequest;
import com.expense.entity.Budget;
import com.expense.interceptor.AuthInterceptor;
import com.expense.service.BudgetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 预算管理控制器
 * 
 * 功能说明：
 * - 提供月度预算的设置和查询接口
 * - 支持查询预算使用进度
 * - 用于记账时提示预算剩余额度和超支提醒
 */
@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    /**
     * 获取指定月份的预算
     * 包含预算总额和使用进度
     * 
     * 请求示例：
     * GET /api/budget?month=2026-06
     * 
     * 返回示例：
     * {
     *   "amount": 5000,
     *   "used": 3200,
     *   "remaining": 1800,
     *   "progress": 0.64
     * }
     */
    @GetMapping
    public Result<Map<String, Object>> getBudget(
            @RequestParam String month,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        Budget budget = budgetService.getBudget(userId, month);

        Map<String, Object> data = new HashMap<>();
        if (budget != null) {
            data.put("amount", budget.getAmount());

            // 计算已使用金额
            BigDecimal progress = budgetService.getBudgetProgress(userId, month);
            if (progress != null) {
                BigDecimal used = budget.getAmount().multiply(progress).setScale(2, BigDecimal.ROUND_HALF_UP);
                data.put("used", used);
                data.put("remaining", budget.getAmount().subtract(used));
                data.put("progress", progress);
            } else {
                data.put("used", BigDecimal.ZERO);
                data.put("remaining", budget.getAmount());
                data.put("progress", BigDecimal.ZERO);
            }
        }

        return Result.success(data);
    }

    /**
     * 设置月度预算
     * 如果该月已有预算则更新，否则新建
     * 
     * 请求示例：
     * POST /api/budget
     * {
     *   "yearMonth": "2026-06",
     *   "amount": 5000
     * }
     */
    @PostMapping
    public Result<Void> setBudget(
            @Valid @RequestBody BudgetSetRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        budgetService.setBudget(userId, request);
        return Result.success();
    }
}
