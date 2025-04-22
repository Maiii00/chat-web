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
    private final ObjectMapper objectMapper;
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

    // 快取最近聊天對象
    public void chatListCache(String userId, String senderId, String messageId) {
        String key = "chat-list:" + userId;
        long score = extractTimestampFromId(messageId);
        // messageId 可代表發送時間（Mongo ObjectId 的時間戳）
        redisTemplate.opsForZSet().add(key, senderId, score);
        // 可以保留最近 50 人
        redisTemplate.opsForZSet().removeRange(key, 0, -51);
    }

    // 查詢快取聊天列表
    public List<String> getChatListFromRedis(String userId) {
        String key = "chat-list:" + userId;
        return redisTemplate.opsForZSet().reverseRange(key, 0, MAX_CACHE_SIZE - 1)
                             .stream().toList();
    }
    
    

    // 支援「分頁」的聊天記錄 查詢 Redis 中的最近聊天記錄
    public List<String> getCachedMessages(Message message, int page, int size) {
        String key = generateKey(message);
        int start = page * size;
        int end = start + size - 1;
        return redisTemplate.opsForList().range(key, start, end);
    }

    // 刪除 Redis 聊天快取
    public void deleteMessageFromCache(Message message) {
        String key = generateKey(message);
        try {
            String msgJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().remove(key, 1, msgJson); // 移除第一個匹配值
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing message to JSON", e);
        }
    }
    

    // 產生聊天 Key，確保雙向溝通的 Key 一致
    private String generateKey(Message message) {
        String user1 = message.getSenderId();
        String user2 = message.getReceiverId();
        return "chat:" + (user1.compareTo(user2) < 0 ? user1 + ":" + user2 : user2 + ":" + user1);
    }

    // 將 MongoDB ObjectId 提取 timestamp 當作排序值
    private long extractTimestampFromId(String objectId) {
        return new org.bson.types.ObjectId(objectId).getTimestamp();
    }
}
