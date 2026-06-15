package com.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.expense.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 
 * 功能说明：
 * - 继承 BaseMapper，提供基础 CRUD 操作
 * - 根据 openid 查询用户（微信登录时使用）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // MyBatis-Plus 的 BaseMapper 已提供以下方法：
    // insert, deleteById, updateById, selectById, selectList 等

    // 如需自定义查询，可在此添加方法，并在 resources/mapper/ 下编写 XML
}
