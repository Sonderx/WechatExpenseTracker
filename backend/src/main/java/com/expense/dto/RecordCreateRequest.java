package com.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建账目记录请求 DTO
 * 
 * 功能说明：
 * - 接收前端提交的记账数据
 * - 使用 Jakarta Validation 注解进行参数校验
 */
@Data
public class RecordCreateRequest {

    /**
     * 金额：正数=收入，负数=支出
     * 不为零
     */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    /**
     * 分类类型：0=支出，1=收入
     */
    @NotNull(message = "分类类型不能为空")
    private Integer type;

    /**
     * 分类ID
     */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /**
     * 记账时间
     * 不为空时使用指定时间，为空时使用当前时间
     */
    private LocalDateTime recordTime;

    /**
     * 备注说明（可选）
     */
    private String remark;
}
