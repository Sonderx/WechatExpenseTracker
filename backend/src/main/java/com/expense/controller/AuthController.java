package com.expense.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.expense.common.BusinessException;
import com.expense.common.Result;
import com.expense.dto.LoginRequest;
import com.expense.entity.User;
import com.expense.interceptor.AuthInterceptor;
import com.expense.service.UserService;
import com.expense.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证控制器
 * 
 * 功能说明：
 * - 处理微信小程序登录请求
 * - 本地开发时支持 mock 模式
 * - 登录成功后返回 JWT Token
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /** 小程序 AppID */
    @Value("${wx.mini.app-id}")
    private String appId;

    /** 小程序 AppSecret */
    @Value("${wx.mini.app-secret}")
    private String appSecret;

    /** 是否使用 mock 模式 */
    @Value("${wx.mini.mock-openid}")
    private boolean mockOpenid;

    /**
     * 微信登录接口
     * 
     * 本地开发时：
     *   - 设置 mock-openid=true
     *   - 传入 mockOpenid 参数即可登录
     * 
     * 线上环境：
     *   - 小程序调用 wx.login() 获取 code
     *   - 后端用 code 换取 openid
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String openid;

        if (mockOpenid) {
            // 本地开发模式：使用传入的 mockOpenid
            openid = request.getMockOpenid();
            if (openid == null || openid.isEmpty()) {
                openid = "mock_user_001";  // 默认 mock 用户
            }
            log.info("使用 mock 模式登录，openid: {}", openid);
        } else {
            // 线上模式：用微信 code 换取 openid
            if (request.getCode() == null || request.getCode().isEmpty()) {
                throw new BusinessException(BusinessException.WX_LOGIN_FAIL, "登录凭证不能为空");
            }
            openid = getOpenidFromWx(request.getCode());
        }

        // 查找或创建用户
        User user = userService.findByOpenid(openid);
        if (user == null) {
            user = userService.createUser(openid);
            log.info("新用户注册，userId: {}", user.getId());
        }

        // 更新昵称和头像（如果前端有传）
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        userService.updateUser(user);

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId());

        // 返回 Token 和用户信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());

        return Result.success(data);
    }

    /**
     * 通过微信 code 换取 openid
     * 调用微信 auth.code2Session 接口
     */
    private String getOpenidFromWx(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code
        );

        try {
            log.info("调用微信登录接口: appId={}, code={}", appId, code);
            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);
            log.info("微信登录响应: {}", result);

            String openid = json.getStr("openid");
            if (openid == null || openid.isEmpty()) {
                String errMsg = json.getStr("errmsg", "未知错误");
                int errCode = json.getInt("errcode", 0);
                log.error("微信登录失败: errcode={}, errmsg={}", errCode, errMsg);
                throw new BusinessException(BusinessException.WX_LOGIN_FAIL, "微信登录失败: " + errMsg);
            }
            log.info("微信登录成功: openid={}", openid);
            return openid;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信登录接口异常", e);
            throw new BusinessException(BusinessException.WX_LOGIN_FAIL, "微信登录接口调用失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(BusinessException.NOT_FOUND, "用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        data.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        return Result.success(data);
    }

    /**
     * 更新用户资料（昵称/头像）
     */
    @PostMapping("/updateProfile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> body,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(BusinessException.NOT_FOUND, "用户不存在");
        }
        if (body.containsKey("nickname")) {
            user.setNickname(body.get("nickname"));
        }
        if (body.containsKey("avatarUrl")) {
            user.setAvatarUrl(body.get("avatarUrl"));
        }
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 上传头像
     * 接收小程序上传的图片文件，保存到服务器，返回可访问的 URL
     */
    @PostMapping("/uploadAvatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);

        // 获取文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成唯一文件名
        String fileName = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + suffix;

        // 保存文件
        String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            // 返回可访问的 URL
            String avatarUrl = "http://localhost:8080/uploads/avatar/" + fileName;
            Map<String, String> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);

            // 更新用户头像
            User user = userService.findById(userId);
            if (user != null) {
                user.setAvatarUrl(avatarUrl);
                userService.updateUser(user);
            }

            return Result.success(data);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException(9999, "头像上传失败");
        }
    }
}
