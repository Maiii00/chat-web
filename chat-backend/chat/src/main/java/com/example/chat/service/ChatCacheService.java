package com.example.chat.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.chat.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_CACHE_SIZE = 50;

    // 儲存聊天記錄到 Redis
    public void cacheMessage(Message message) {
        String key = generateKey(message);
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().leftPush(key, messageJson);
            redisTemplate.opsForList().trim(key, 0, MAX_CACHE_SIZE - 1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing message to JSON", e);
        }
    }

    // 支援「分頁」的聊天記錄 查詢 Redis 中的最近聊天記錄
    public List<String> getCachedMessages(Message message, int page, int size) {
        String key = generateKey(message);
        int start = page * size;
        int end = start + size - 1;
        return redisTemplate.opsForList().range(key, start, end);
    }

    // 刪除 Redis 聊天快取（當用戶手動刪除聊天時）
    public void clearChatCache(Message message) {
        String key = generateKey(message);
        redisTemplate.delete(key);
    }

    // 產生聊天 Key，確保雙向溝通的 Key 一致
    private String generateKey(Message message) {
        String user1 = message.getSenderId();
        String user2 = message.getReceiverId();
        return "chat:" + (user1.compareTo(user2) < 0 ? user1 + ":" + user2 : user2 + ":" + user1);
    }
}
