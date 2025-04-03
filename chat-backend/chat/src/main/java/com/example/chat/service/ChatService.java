package com.example.chat.service;

import java.util.Set;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.chat.model.Message;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatService {
    
    private final MessageService messageService;
    private final GroupService groupService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 訊息推送工具
    private final ChatCacheService chatCacheService;

    @MessageMapping("/group") // 客戶端發送到 "/app/group"
    public void sendToGroup(@Payload Message message) {
        messageService.saveMessage(message);

        Set<String> members = groupService.getGroupMembers(message.getReceiverId());
        for (String member : members) {
            messagingTemplate.convertAndSendToUser(member, "/queue/messages", message);
        }
    }

    // 私聊
    @MessageMapping("/private") // 客戶端發送到 "/app/private"
    public void sendToUser(@Payload Message message) {
        messageService.saveMessage(message); // 儲存 MongoDB
        chatCacheService.cacheMessage(message); // 存 Redis

        // 只發送給指定的用戶 "/user/{receiver}/queue/messages"
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId(), "/queue/messages", message
        );
    }
}
