package com.expense.common;

import lombok.Data;

/**
 * 统一响应结果类
 * 
 * 功能说明：
 * - 所有 API 接口统一返回此格式
 * - code=0 表示成功，code!=0 表示失败
 * 
 * 响应格式示例：
 * 成功：{ "code": 0, "data": { ... }, "msg": "success" }
 * 失败：{ "code": 1001, "data": null, "msg": "参数错误" }
 */
@Data
public class Result<T> {

    /** 响应码：0=成功，其他为错误码 */
    private int code;

    /** 响应数据 */
    private T data;

    /** 响应消息 */
    private String msg;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg("success");
        return result;
    }

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setData(data);
        result.setMsg("success");
        return result;
    }

    /**
     * 失败响应
     * @param code 错误码
     * @param msg 错误消息
     */
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
