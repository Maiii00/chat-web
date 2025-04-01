package com.example.chat.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    // 設置 JWT 過期時間 (單位：毫秒) - 15 分鐘
    private static final long ACCESS_EXPIRATION_TIME = 15 * 60 * 1000;
    // 設置 週期較長 JWT - 7 天
    private static final long REFRESH_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    // 生成一個 Secret Key
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 生成 Access Token
    public String generateAccessToken(String username) {
        return generateToken(username, ACCESS_EXPIRATION_TIME);
    }

    // 生成 Refresh Token
    public String generateRefreshToken(String username) {
        return generateToken(username, REFRESH_EXPIRATION_TIME);
    }

    // 生成 JWT Token
    public String generateToken(String username, long expirationTime) {
        return Jwts.builder()
                    .setSubject(username) // 設置 Token 主體（用戶名）
                    .setIssuedAt(new Date()) // 設置簽發時間
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 設置過期時間
                    .signWith(secretKey) // 使用密鑰簽名
                    .compact();
    }

    // 解析 JWT Token 以獲取用戶名
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 設置簽名密鑰
                    .build()
                    .parseClaimsJws(token) // 解析 Token
                    .getBody()
                    .getSubject(); // 取得用戶名
    }

    // 驗證 JWT Token 是否有效
    public boolean validateToken(String token, String username) {
        try {
            return extractUsername(token).equals(username) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    // 檢查 Token 是否過期
    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                                .setSigningKey(secretKey)
                                .build()
                                .parseClaimsJws(token)
                                .getBody()
                                .getExpiration();
        return expiration.before(new Date());
    }

    // 取得 Token 剩餘時間（毫秒）
    public long getExpirationMillis(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                                    .setSigningKey(secretKey)
                                    .build()
                                    .parseClaimsJws(token)
                                    .getBody()
                                    .getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (JwtException e) {
            return 0; // 無效 Token，視為已過期
        }
    }
}
