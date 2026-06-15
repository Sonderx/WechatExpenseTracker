package com.expense.common;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * 功能说明：
 * - 抛出业务逻辑错误时使用
 * - 包含错误码和错误消息
 * - 由 GlobalExceptionHandler 统一处理
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    /**
     * 构造业务异常
     * @param code 错误码
     * @param msg 错误消息
     */
    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * 常用错误码定义
     */
    public static final int PARAM_ERROR = 1001;       // 参数错误
    public static final int NOT_FOUND = 1002;          // 资源不存在
    public static final int UNAUTHORIZED = 1003;       // 未登录
    public static final int FORBIDDEN = 1004;          // 无权限
    public static final int WX_LOGIN_FAIL = 1005;      // 微信登录失败
    public static final int CATEGORY_HAS_RECORDS = 1006; // 分类下有记录，无法删除
}
