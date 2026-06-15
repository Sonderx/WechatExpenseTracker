package com.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账目记录 VO（包含分类信息）
 * 
 * 功能说明：
 * - 返回给前端的记录数据
 * - 包含分类名称和图标（通过 JOIN 查询获得）
 */
@Data
public class RecordVO {

    /** 记录ID */
    private Long id;

    /** 金额：正数=收入，负数=支出 */
    private BigDecimal amount;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称（来自 categories 表） */
    private String categoryName;

    /** 分类图标（来自 categories 表） */
    private String categoryIcon;

    /** 记账时间 */
    private LocalDateTime recordTime;

    /** 备注 */
    private String remark;

    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
