package com.example.chat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.model.User;
import com.example.chat.security.JwtUtil;
import com.example.chat.service.LogoutService;
import com.example.chat.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LogoutService logoutService;
    private final JwtUtil jwtUtil;

    // 註冊新用戶
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        String result = userService.register(user);
        if (result.equals("Username already taken")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
    
    // 用戶登入
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User loginUser) {
        try {
            Map<String, String> tokens = userService.login(loginUser);
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // 更新token
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            // 驗證 Refresh Token
            String username = jwtUtil.extractUsername(refreshToken);

            if (jwtUtil.validateToken(refreshToken, username)) {
                // 根據 username 查找 User 實體
                User user = userService.findByUsername(username)
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                // 產出新的 accessToken
                String newAccessToken = jwtUtil.generateAccessToken(user);
                return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
            } else {
                System.out.println("refreshToken 驗證失敗");
                return ResponseEntity.status(403).body(Map.of("error", "Invalid refresh token"));
            }
        } catch (Exception e) {
            System.out.println("refreshToken 異常：" + e.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", "Token expired or invalid"));
        }
    }
    
    // 登出 API
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.badRequest().body(Map.of("message","Invalid or expired token"));
        }

        long expirationMillis = jwtUtil.getExpirationMillis(token);
        logoutService.invalidateToken(token, expirationMillis);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // 從請求頭中提取 JWT
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // 回傳username
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getUsernameById(@PathVariable String id) {
        return userService.findById(id)
            .map(user -> ResponseEntity.ok(Map.of("username", user.getUsername())))
            .orElse(ResponseEntity.notFound().build());
    }

    // 尋找用戶
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String currentUsername = principal.getName();
        List<User> results = userService.searchUsers(keyword)
                                        .stream()
                                        .filter(user -> !user.getUsername().equals(currentUsername)) // 不回傳自己
                                        .toList();
        return ResponseEntity.ok(results);
    }
}
