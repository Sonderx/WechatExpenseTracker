package com.expense.config;

import com.expense.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 
 * 功能说明：
 * - 配置跨域资源共享 (CORS)，允许小程序跨域请求
 * - 注册认证拦截器，保护需要登录的接口
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 配置跨域规则
     * 允许所有来源访问（开发阶段），生产环境应限制具体域名
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的 HTTP 方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带凭证（Cookie 等）
                .maxAge(3600);  // 预检请求缓存时间（秒）
    }

    /**
     * 注册拦截器
     * 对需要登录的 API 路径进行 Token 校验
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 /api/ 开头的请求
                .excludePathPatterns(
                        "/api/auth/login"  // 排除登录接口
                );
    }

    /**
     * 配置静态资源映射
     * 上传的头像文件可通过 /uploads/avatar/xxx.jpg 访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
