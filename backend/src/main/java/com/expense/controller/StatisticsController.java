package com.expense.controller;

import com.expense.common.Result;
import com.expense.interceptor.AuthInterceptor;
import com.expense.mapper.RecordMapper;
import com.expense.vo.CategoryStatsVO;
import com.expense.vo.MonthlySummaryVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计报表控制器
 * 
 * 功能说明：
 * - 提供月度收支汇总（柱状图数据）
 * - 提供分类支出统计（饼图数据）
 * - 数据用于小程序的统计图表展示
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private RecordMapper recordMapper;

    /**
     * 获取年度月度收支汇总
     * 返回指定年份每个月的收入和支出总额
     * 用于柱状图展示（收入 vs 支出双柱对比）
     * 
     * 请求示例：
     * GET /api/statistics/monthly?year=2026
     * 
     * 返回示例：
     * [
     *   { "yearMonth": "2026-01", "income": 8000, "expense": -5200 },
     *   { "yearMonth": "2026-02", "income": 8000, "expense": -4800 },
     *   ...
     * ]
     */
    @GetMapping("/monthly")
    public Result<List<MonthlySummaryVO>> getMonthlySummary(
            @RequestParam Integer year,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        List<MonthlySummaryVO> summary = recordMapper.selectMonthlySummary(userId, year);
        return Result.success(summary);
    }

    /**
     * 获取月度分类支出统计
     * 返回指定月份各分类的支出总额和记录数
     * 用于饼图展示（各分类支出占比）
     * 
     * 请求示例：
     * GET /api/statistics/category?month=2026-06
     * 
     * 返回示例：
     * [
     *   { "categoryId": 1, "categoryName": "餐饮", "categoryIcon": "🍜", "total": 1200, "count": 15 },
     *   { "categoryId": 2, "categoryName": "交通", "categoryIcon": "🚌", "total": 300, "count": 8 },
     *   ...
     * ]
     */
    @GetMapping("/category")
    public Result<List<CategoryStatsVO>> getCategoryStats(
            @RequestParam String month,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        List<CategoryStatsVO> stats = recordMapper.selectCategoryStats(userId, month);
        return Result.success(stats);
    }

    /**
     * 获取当月收支概览
     * 返回当月总收入、总支出、结余
     * 用于首页顶部的月度汇总卡片
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getMonthlyOverview(
            @RequestParam String month,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);

        BigDecimal income = recordMapper.sumIncomeByMonth(userId, month);
        BigDecimal expense = recordMapper.sumExpenseByMonth(userId, month);

        // 结余 = 收入 + 支出（支出为负数，所以加法）
        BigDecimal balance = BigDecimal.ZERO;
        if (income != null && expense != null) {
            balance = income.add(expense);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("income", income != null ? income : BigDecimal.ZERO);
        data.put("expense", expense != null ? expense.abs() : BigDecimal.ZERO);  // 返回正数，前端显示时加负号
        data.put("balance", balance);

        return Result.success(data);
    }
}
