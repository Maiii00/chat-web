package com.example.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import com.example.chat.model.Message;
import com.example.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/group") // 客戶端發送到 "/app/group"
    public void sendToGroup(@Payload Message message) {
        chatService.sendToGroup(message);
    }

    @MessageMapping("/private") // 客戶端發送到 "/app/private"
    public void sendToUser(@Payload Message message) {
        chatService.sendToUser(message);
    }
}
