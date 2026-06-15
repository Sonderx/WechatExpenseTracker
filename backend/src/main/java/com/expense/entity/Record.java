package com.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账目记录实体类
 * 
 * 功能说明：
 * - 映射 records 表
 * - 存储每笔收支记录
 * - 金额约定：正数=收入，负数=支出
 */
@Data
@TableName("records")
public class Record {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 金额：正数=收入，负数=支出 */
    private BigDecimal amount;

    /** 分类ID，关联 categories 表 */
    private Long categoryId;

    /** 记账时间（用户选择的日期时间） */
    private LocalDateTime recordTime;

    /** 备注说明（可选） */
    private String remark;

    /** 记录创建时间（系统自动填充） */
    private LocalDateTime createdAt;
}
