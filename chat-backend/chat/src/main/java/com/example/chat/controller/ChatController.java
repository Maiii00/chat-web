package com.example.chat.controller;

import java.util.Set;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.example.chat.model.Message;
import com.example.chat.service.GroupService;
import com.example.chat.service.MessageService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class ChatController {

    private final MessageService messageService;
    private final GroupService groupService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 訊息推送工具

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
        messageService.saveMessage(message);

        // 只發送給指定的用戶 "/user/{receiver}/queue/messages"
        messagingTemplate.convertAndSendToUser(message.getReceiverId(), "/queue/messages", message);
    }
}
