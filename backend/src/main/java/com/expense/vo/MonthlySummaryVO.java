package com.expense.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 月度收支汇总 VO
 * 
 * 功能说明：
 * - 用于年度月度收支统计接口
 * - 每条记录代表一个月的收支汇总
 */
@Data
public class MonthlySummaryVO {

    /** 月份，格式：2026-06 */
    private String yearMonth;

    /** 当月收入总额 */
    private BigDecimal income;

    /** 当月支出总额（负数） */
    private BigDecimal expense;
}
