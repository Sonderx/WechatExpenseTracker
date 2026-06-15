package com.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * 功能说明：
 * - 映射 users 表
 * - 存储微信小程序用户信息
 * - 通过 openid 关联微信账号
 */
@Data
@TableName("users")
public class User {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信小程序唯一标识（openid） */
    private String openid;

    /** 微信昵称 */
    private String nickname;

    /** 微信头像URL */
    private String avatarUrl;

    /** 注册时间 */
    private LocalDateTime createdAt;
}
