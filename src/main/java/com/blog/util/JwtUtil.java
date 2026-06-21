package com.blog.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    // 密钥（建议32位以上，生产环境放配置文件，不要硬编码）
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    // Token 过期时间：24小时（单位：毫秒）
    @Value("${jwt.expire}")
    private long EXPIRATION_TIME;

    /**
     * 获取加密密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * 生成 JWT Token
     * @param claims 要存储的用户信息（如userId、username）
     * @return token字符串
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims) // 存储自定义数据
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(getSecretKey()) // 签名
                .compact();
    }

    /**
     * 解析 Token 获取存储的数据
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }

    public Long extractId(String token) {
        return extractClaims(token).get("id", Long.class);
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            // 新增：校验 Token 是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
