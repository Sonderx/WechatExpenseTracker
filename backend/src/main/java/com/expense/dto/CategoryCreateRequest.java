package com.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建/修改分类请求 DTO
 * 
 * 功能说明：
 * - 接收前端提交的分类数据
 * - 支持创建新分类和修改已有分类
 */
@Data
public class CategoryCreateRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 分类类型：0=支出，1=收入
     */
    @NotNull(message = "分类类型不能为空")
    private Integer type;

    /**
     * 分类图标（emoji 字符）
     */
    private String icon;

    /**
     * 排序序号（可选）
     */
    private Integer sortOrder;
}
