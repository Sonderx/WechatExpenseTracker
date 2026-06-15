package com.expense.interceptor;

import com.expense.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 
 * 功能说明：
 * - 拦截所有需要登录的 API 请求
 * - 从请求头中提取 JWT Token 并校验
 * - 解析出 userId 并存入 request attribute，供后续 Controller 使用
 * - Token 无效或缺失时返回 401 未授权
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** JWT 工具类 */
    @Autowired
    private JwtUtil jwtUtil;

    /** 在 request 中存放 userId 的 key */
    public static final String USER_ID_KEY = "currentUserId";

    /**
     * 请求处理前的拦截逻辑
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器
     * @return true=继续处理，false=拦截请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头中获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少 Authorization 请求头");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1003,\"msg\":\"未登录，请先登录\"}");
            return false;
        }

        // 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(7);

        // 解析 Token 获取 userId
        Long userId = jwtUtil.parseUserId(token);
        if (userId == null) {
            log.warn("Token 无效或已过期");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1003,\"msg\":\"Token 已过期，请重新登录\"}");
            return false;
        }

        // 将 userId 存入 request attribute，供 Controller 获取
        request.setAttribute(USER_ID_KEY, userId);
        return true;
    }
}
