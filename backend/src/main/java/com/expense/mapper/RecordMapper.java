package com.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.expense.entity.Record;
import com.expense.vo.CategoryStatsVO;
import com.expense.vo.MonthlySummaryVO;
import com.expense.vo.RecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账目记录 Mapper 接口
 * 
 * 功能说明：
 * - 继承 BaseMapper，提供基础 CRUD 操作
 * - 支持按月分页查询
 * - 支持统计查询（月度汇总、分类统计）
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

    /**
     * 查询用户指定月份的月度支出总额
     * 
     * @param userId 用户ID
     * @param yearMonth 月份，格式：2026-06
     * @return 支出总额（负数）
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM records " +
            "WHERE user_id = #{userId} " +
            "AND DATE_FORMAT(record_time, '%Y-%m') = #{yearMonth} " +
            "AND amount < 0")
    BigDecimal sumExpenseByMonth(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

    /**
     * 查询用户指定月份的月度收入总额
     * 
     * @param userId 用户ID
     * @param yearMonth 月份，格式：2026-06
     * @return 收入总额（正数）
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM records " +
            "WHERE user_id = #{userId} " +
            "AND DATE_FORMAT(record_time, '%Y-%m') = #{yearMonth} " +
            "AND amount > 0")
    BigDecimal sumIncomeByMonth(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

    /**
     * 查询用户指定年份每月的收支汇总
     * 
     * @param userId 用户ID
     * @param year 年份，如 2026
     * @return 每月收支汇总列表
     */
    @Select("SELECT DATE_FORMAT(record_time, '%Y-%m') AS yearMonth, " +
            "SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) AS income, " +
            "SUM(CASE WHEN amount < 0 THEN amount ELSE 0 END) AS expense " +
            "FROM records " +
            "WHERE user_id = #{userId} " +
            "AND YEAR(record_time) = #{year} " +
            "GROUP BY DATE_FORMAT(record_time, '%Y-%m') " +
            "ORDER BY yearMonth ASC")
    List<MonthlySummaryVO> selectMonthlySummary(@Param("userId") Long userId, @Param("year") Integer year);

    /**
     * 查询用户指定月份各分类的支出统计
     * 
     * @param userId 用户ID
     * @param yearMonth 月份，格式：2026-06
     * @return 各分类支出统计列表
     */
    @Select("SELECT c.id AS categoryId, c.name AS categoryName, c.icon AS categoryIcon, " +
            "SUM(ABS(r.amount)) AS total, COUNT(*) AS count " +
            "FROM records r " +
            "INNER JOIN categories c ON r.category_id = c.id " +
            "WHERE r.user_id = #{userId} " +
            "AND DATE_FORMAT(r.record_time, '%Y-%m') = #{yearMonth} " +
            "AND r.amount < 0 " +
            "GROUP BY c.id, c.name, c.icon " +
            "ORDER BY total DESC")
    List<CategoryStatsVO> selectCategoryStats(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

    /**
     * 检查指定分类下是否有账目记录
     * 
     * @param categoryId 分类ID
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM records WHERE category_id = #{categoryId}")
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 分页查询记录（JOIN categories 获取分类名称和图标）
     * 
     * @param page 分页参数
     * @param userId 用户ID
     * @param yearMonth 月份，格式：2026-06
     * @return 分页结果（包含分类信息）
     */
    @Select("SELECT r.id, r.amount, r.category_id AS categoryId, " +
            "c.name AS categoryName, c.icon AS categoryIcon, " +
            "r.record_time AS recordTime, r.remark, r.created_at AS createdAt " +
            "FROM records r " +
            "LEFT JOIN categories c ON r.category_id = c.id " +
            "WHERE r.user_id = #{userId} " +
            "AND DATE_FORMAT(r.record_time, '%Y-%m') = #{yearMonth} " +
            "ORDER BY r.record_time DESC")
    IPage<RecordVO> selectPageWithCategory(Page<RecordVO> page,
                                           @Param("userId") Long userId,
                                           @Param("yearMonth") String yearMonth);
}
