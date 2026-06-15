package com.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.expense.entity.User;
import com.expense.mapper.UserMapper;
import com.expense.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 
 * 功能说明：
 * - 实现用户相关的业务操作
 * - 微信登录时查找或创建用户
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 通过 openid 查找用户
     * 使用 LambdaQueryWrapper 构建查询条件
     */
    @Override
    public User findByOpenid(String openid) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid)
        );
    }

    /**
     * 通过 ID 查找用户
     */
    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 创建新用户
     * 微信登录时，如果用户不存在则自动创建
     */
    @Override
    public User createUser(String openid) {
        User user = new User();
        user.setOpenid(openid);
        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户信息
     */
    @Override
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}
