package com.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体类
 * 
 * 功能说明：
 * - 映射 categories 表
 * - 支持支出（type=0）和收入（type=1）两大类
 * - 系统预置分类（is_system=true）和用户自定义分类
 */
@Data
@TableName("categories")
public class Category {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID，0表示系统预置分类 */
    private Long userId;

    /** 分类名称 */
    private String name;

    /** 分类类型：0=支出，1=收入 */
    private Integer type;

    /** 分类图标（emoji 字符） */
    private String icon;

    /** 是否系统预置（系统预置分类不可删除） */
    private Boolean isSystem;

    /** 是否隐藏（用户可隐藏不需要的分类） */
    private Boolean isHidden;

    /** 排序序号（越小越靠前） */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
