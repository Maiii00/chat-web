package com.example.chat.service;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UnreadMessageService {
    private final RedisTemplate<String, Object> redisTemplate;

    public UnreadMessageService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 增加未讀消息數量
    public void incrementUnreadCount(String senderId, String receiverId) {
        try {
            String key = "unread:" + receiverId;
            redisTemplate.opsForHash().increment(key, senderId, 1);
        } catch (Exception e) {
            System.err.println("Redis increment failed for receiver: " + receiverId + ", sender:" + senderId + ", error: " + e.getMessage());
        }
    }

    // 獲取未讀消息數量
    public int getUnreadCount(String senderId, String receiverId) {
        try {
            String key = "unread:" + receiverId;
            Object count = redisTemplate.opsForHash().get(key, senderId);
            return count != null ? (int) count : 0;
        } catch (Exception e) {
            System.err.println("Redis get unread count failed for receiver: " + receiverId + ", sender:" + senderId + ", error: " + e.getMessage());
            return 0; // Redis 失敗時返回 0
        }
    }

    // 清除未讀
    public void clearUnreadCount(String senderId, String receiverId) {
        try {
            String key = "unread:" + receiverId;
            redisTemplate.opsForHash().delete(key, senderId);
        } catch (Exception e) {
            System.err.println("Redis clear unread count failed for receiver: " + receiverId + ", sender: " + senderId + ", error: " + e.getMessage());
        }
    }

    // 取得所有用戶未讀
    public Map<Object, Object> getAllUnreadCount(String receiverId) {
        String key = "unread:" + receiverId;
        return redisTemplate.opsForHash().entries(key);
    }
}
