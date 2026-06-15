package com.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 设置月度预算请求 DTO
 * 
 * 功能说明：
 * - 接收前端提交的预算数据
 * - 每个用户每月只能设置一个预算
 */
@Data
public class BudgetSetRequest {

    /**
     * 预算月份，格式：2026-06
     */
    @NotBlank(message = "预算月份不能为空")
    private String yearMonth;

    /**
     * 预算金额
     */
    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    private BigDecimal amount;
}
