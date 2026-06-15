package com.expense.controller;

import com.expense.common.Result;
import com.expense.dto.CategoryCreateRequest;
import com.expense.entity.Category;
import com.expense.interceptor.AuthInterceptor;
import com.expense.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 * 
 * 功能说明：
 * - 提供分类的查询、创建、修改、删除接口
 * - 查询时返回系统预置 + 用户自定义的合并列表
 * - 创建/修改/删除操作需要登录
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     * 返回系统预置分类和当前用户自定义分类的合并列表
     * 
     * 请求示例：
     * GET /api/categories?type=0  （只查支出分类）
     * GET /api/categories?type=1  （只查收入分类）
     * GET /api/categories         （查全部）
     */
    @GetMapping
    public Result<List<Category>> listCategories(
            @RequestParam(required = false) Integer type,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        List<Category> categories = categoryService.listCategories(userId, type);
        return Result.success(categories);
    }

    /**
     * 创建自定义分类
     * 
     * 请求示例：
     * POST /api/categories
     * {
     *   "name": "健身",
     *   "type": 0,
     *   "icon": "🏋️"
     * }
     */
    @PostMapping
    public Result<Category> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        Category category = categoryService.createCategory(userId, request);
        return Result.success(category);
    }

    /**
     * 修改分类
     * 只能修改用户自己的自定义分类
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        categoryService.updateCategory(userId, id, request);
        return Result.success();
    }

    /**
     * 删除分类
     * 系统预置分类不可删除，有记录的分类不可删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        categoryService.deleteCategory(userId, id);
        return Result.success();
    }
}
