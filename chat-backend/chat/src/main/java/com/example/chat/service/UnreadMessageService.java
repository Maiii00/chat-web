package com.example.chat.service;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UnreadMessageService {
    private static final String UNREAD_KEY = "unread_messages";
    private final RedisTemplate<String, Object> redisTemplate;

    public UnreadMessageService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 增加未讀消息數量
    public void incrementUnreadCount(String user) {
        try {
            redisTemplate.opsForHash().increment(UNREAD_KEY, user, 1);
        } catch (Exception e) {
            System.err.println("Redis increment failed for user: " + user + ", error: " + e.getMessage());
        }
    }

    // 獲取未讀消息數量
    public int getUnreadCount(String user) {
        try {
            Object count = redisTemplate.opsForHash().get(UNREAD_KEY, user);
            return count != null ? (int) count : 0;
        } catch (Exception e) {
            System.err.println("Redis get unread count failed for user: " + user + ", error: " + e.getMessage());
            return 0; // Redis 失敗時返回 0
        }
    }

    // 清除未讀
    public void clearUnreadCount(String user) {
        try {
            redisTemplate.opsForHash().delete(UNREAD_KEY, user);
        } catch (Exception e) {
            System.err.println("Redis clear unread count failed for user: " + user + ", error: " + e.getMessage());
        }
    }

    // 取得所有用戶未讀
    public Map<Object, Object> getAllUnreadCount() {
        return redisTemplate.opsForHash().entries(UNREAD_KEY);
    }
}
