package com.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.expense.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper 接口
 * 
 * 功能说明：
 * - 继承 BaseMapper，提供基础 CRUD 操作
 * - 支持查询系统预置分类和用户自定义分类的合并列表
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询用户的分类列表（系统预置 + 用户自定义）
     * 
     * @param userId 用户ID
     * @param type 分类类型：0=支出，1=收入，null=全部
     * @return 分类列表（按 sort_order 排序）
     */
    @Select("<script>" +
            "SELECT * FROM categories " +
            "WHERE (user_id = 0 OR user_id = #{userId}) " +
            "AND is_hidden = FALSE " +
            "<if test='type != null'> AND type = #{type} </if>" +
            "ORDER BY type ASC, sort_order ASC" +
            "</script>")
    List<Category> selectByUserId(@Param("userId") Long userId, @Param("type") Integer type);
}
