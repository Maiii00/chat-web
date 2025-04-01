package com.example.chat.service;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final RedisTemplate<String, String> redisTemplate;

    private String getGroupKey(String groupId) {
        return "group:" + groupId;
    }

    // 群組加入成員
    public boolean addUserToGroup(String groupId, String userId) {
        Long added = redisTemplate.opsForSet().add(getGroupKey(groupId), userId);
        return added != null && added > 0;
    }

    //群組退出成員
    public boolean removeUserFromGroup(String groupId, String userId) {
        Long removed = redisTemplate.opsForSet().remove(getGroupKey(groupId), userId);
        return removed != null && removed > 0;
    }

    //獲取群組成員
    public Set<String> getGroupMembers(String groupId) {
        return redisTemplate.opsForSet().members(getGroupKey(groupId) + groupId);
    }
}
