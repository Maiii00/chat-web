package com.example.chat.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.example.chat.model.Message;
import com.example.chat.service.MessageService;

@Controller
public class ChatController {

    private final MessageService messageService;

    public ChatController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat") // 接收來自 "/app/chat" 的訊息
    @SendTo("/topic/messages") // 推播訊息給訂閱 "/topic/messages" 的客戶端
    public Message sendMessage(@Payload Message message) {
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageService.saveMessage(message.getSender(), message.getReceiver(), message.getContent());
        // 儲存到資料庫
        return savedMessage;
    }
}
