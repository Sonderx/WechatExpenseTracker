package com.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算实体类
 * 
 * 功能说明：
 * - 映射 budgets 表
 * - 存储用户月度预算设置
 * - 每个用户每月只能设置一个预算
 */
@Data
@TableName("budgets")
public class Budget {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 预算月份，格式：2026-06（使用反引号转义保留字） */
    @TableField(value = "`year_month`")
    private String yearMonth;

    /** 预算金额 */
    private BigDecimal amount;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
