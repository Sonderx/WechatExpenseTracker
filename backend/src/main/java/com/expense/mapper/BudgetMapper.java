package com.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.expense.entity.Budget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算 Mapper 接口
 * 
 * 功能说明：
 * - 继承 BaseMapper，提供基础 CRUD 操作
 * - 支持按用户和月份查询预算
 */
@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {

    // MyBatis-Plus 的 BaseMapper 已提供以下方法：
    // insert, deleteById, updateById, selectById, selectList 等

    // 预算查询可通过 LambdaQueryWrapper 实现：
    // budgetMapper.selectOne(new LambdaQueryWrapper<Budget>()
    //     .eq(Budget::getUserId, userId)
    //     .eq(Budget::getYearMonth, yearMonth));
}
