package com.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.expense.common.BusinessException;
import com.expense.dto.CategoryCreateRequest;
import com.expense.entity.Category;
import com.expense.mapper.CategoryMapper;
import com.expense.mapper.RecordMapper;
import com.expense.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现类
 * 
 * 功能说明：
 * - 实现分类相关的业务操作
 * - 查询时合并系统预置分类和用户自定义分类
 * - 删除时检查分类下是否有账目记录
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RecordMapper recordMapper;

    /**
     * 获取用户的分类列表
     * 系统预置分类（user_id=0）和用户自定义分类都会返回
     */
    @Override
    public List<Category> listCategories(Long userId, Integer type) {
        return categoryMapper.selectByUserId(userId, type);
    }

    /**
     * 创建自定义分类
     * 新分类绑定到当前用户
     */
    @Override
    public Category createCategory(Long userId, CategoryCreateRequest request) {
        Category category = new Category();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon() != null ? request.getIcon() : "📌");
        category.setIsSystem(false);  // 用户创建的分类不是系统预置
        category.setIsHidden(false);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 99);
        categoryMapper.insert(category);
        return category;
    }

    /**
     * 修改分类
     * 只能修改用户自己的分类，系统预置分类不可修改
     */
    @Override
    public void updateCategory(Long userId, Long categoryId, CategoryCreateRequest request) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(BusinessException.NOT_FOUND, "分类不存在");
        }
        if (category.getIsSystem()) {
            throw new BusinessException(BusinessException.FORBIDDEN, "系统预置分类不可修改");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(BusinessException.FORBIDDEN, "无权修改此分类");
        }

        category.setName(request.getName());
        category.setType(request.getType());
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类
     * 系统预置分类不可删除
     * 分类下有账目记录时不可删除（可隐藏）
     */
    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(BusinessException.NOT_FOUND, "分类不存在");
        }
        if (category.getIsSystem()) {
            throw new BusinessException(BusinessException.FORBIDDEN, "系统预置分类不可删除");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(BusinessException.FORBIDDEN, "无权删除此分类");
        }

        // 检查分类下是否有账目记录
        int count = recordMapper.countByCategoryId(categoryId);
        if (count > 0) {
            throw new BusinessException(BusinessException.CATEGORY_HAS_RECORDS,
                    "该分类下有 " + count + " 条记录，请先删除相关记录或隐藏此分类");
        }

        categoryMapper.deleteById(categoryId);
    }
}
