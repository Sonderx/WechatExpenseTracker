package com.expense.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类支出统计 VO
 * 
 * 功能说明：
 * - 用于月度分类支出统计接口
 * - 每条记录代表一个分类的支出汇总
 */
@Data
public class CategoryStatsVO {

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 分类图标 */
    private String categoryIcon;

    /** 该分类的支出总额 */
    private BigDecimal total;

    /** 该分类的记录条数 */
    private Integer count;
}
