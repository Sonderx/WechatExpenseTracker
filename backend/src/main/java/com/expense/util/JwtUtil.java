package com.expense.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT (JSON Web Token) 工具类
 * 
 * 功能说明：
 * - 生成 JWT Token（登录成功后返回给前端）
 * - 解析 JWT Token（每次请求时校验 Token 有效性）
 * - Token 中包含 userId 信息，用于识别当前用户
 */
@Component
public class JwtUtil {

    /**
     * JWT 签名密钥
     * 从 application-dev.yml 中读取配置
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token 过期时间（毫秒）
     * 默认 7 天
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成签名密钥
     * 使用 HMAC-SHA256 算法
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     * 
     * @param userId 用户ID
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))  // 将 userId 作为 Token 的 subject
                .issuedAt(now)  // 签发时间
                .expiration(expiryDate)  // 过期时间
                .signWith(getSigningKey())  // 使用密钥签名
                .compact();
    }

    /**
     * 从 Token 中解析用户ID
     * 
     * @param token JWT Token
     * @return 用户ID，解析失败返回 null
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            // Token 解析失败（过期、篡改、格式错误等）
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        return parseUserId(token) != null;
    }
}
