package com.example.chat.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import com.example.chat.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    // 註冊新用戶
    public String register(User user) {
        // 檢查用戶名是否已存在
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return "Username already taken";
        }

        user.setPassword(user.getPassword());
        userRepository.save(user);
        return "User registered successfully";
    }

    // 用戶登入
    public Map<String, String> login(User loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                                    .orElseThrow(() -> new RuntimeException("User not found"));

        // 驗證密碼是否匹配
        if (!loginUser.getPassword().equals(user.getPassword())) {
             throw new RuntimeException("Invalid credentials"); // 密碼錯誤
        }
        
        // 生成 JWT 並返回
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // 返回 Token
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
}
