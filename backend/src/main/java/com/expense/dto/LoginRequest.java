package com.expense.dto;

import lombok.Data;

/**
 * 微信登录请求 DTO
 * 
 * 功能说明：
 * - 接收小程序端的登录参数
 * - 本地开发时使用 mockOpenid 模拟登录
 */
@Data
public class LoginRequest {

    /**
     * 微信登录凭证 code
     * 小程序调用 wx.login() 获取
     * 本地 mock 模式下可为空
     */
    private String code;

    /**
     * 本地开发时使用的 mock openid
     * 仅在 mock-openid=true 时生效
     */
    private String mockOpenid;

    /** 微信昵称（可选） */
    private String nickname;

    /** 微信头像URL（可选） */
    private String avatarUrl;
}
