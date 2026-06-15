package com.expense.service;

import com.expense.dto.CategoryCreateRequest;
import com.expense.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 * 
 * 功能说明：
 * - 定义分类相关的业务操作
 * - 支持查询、创建、修改、删除分类
 */
public interface CategoryService {

    /**
     * 获取用户的分类列表（系统预置 + 用户自定义）
     * 
     * @param userId 用户ID
     * @param type 分类类型：0=支出，1=收入，null=全部
     * @return 分类列表
     */
    List<Category> listCategories(Long userId, Integer type);

    /**
     * 创建自定义分类
     * 
     * @param userId 用户ID
     * @param request 分类创建请求
     * @return 新创建的分类
     */
    Category createCategory(Long userId, CategoryCreateRequest request);

    /**
     * 修改分类
     * 
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @param request 分类修改请求
     */
    void updateCategory(Long userId, Long categoryId, CategoryCreateRequest request);

    /**
     * 删除分类
     * 只有用户自定义分类才能删除，且分类下不能有账目记录
     * 
     * @param userId 用户ID
     * @param categoryId 分类ID
     */
    void deleteCategory(Long userId, Long categoryId);
}
