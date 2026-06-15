package com.expense.service;

import com.expense.entity.User;

/**
 * 用户服务接口
 * 
 * 功能说明：
 * - 定义用户相关的业务操作
 * - 微信登录时查找或创建用户
 */
public interface UserService {

    /**
     * 通过 openid 查找用户
     * 
     * @param openid 微信唯一标识
     * @return 用户对象，不存在返回 null
     */
    User findByOpenid(String openid);

    /**
     * 通过 ID 查找用户
     * 
     * @param id 用户ID
     * @return 用户对象，不存在返回 null
     */
    User findById(Long id);

    /**
     * 创建新用户
     * 
     * @param openid 微信唯一标识
     * @return 新创建的用户对象
     */
    User createUser(String openid);

    /**
     * 更新用户信息
     * 
     * @param user 用户对象
     */
    void updateUser(User user);
}
