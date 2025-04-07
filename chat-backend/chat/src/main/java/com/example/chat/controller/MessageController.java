package com.example.chat.controller;

import com.example.chat.dto.ConversationDTO;
import com.example.chat.model.Message;
import com.example.chat.service.MessageService;
import com.example.chat.service.UnreadMessageService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UnreadMessageService unreadMessageService;

    // 送出訊息
    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.saveMessage(message));
    }

    // 查詢歷史訊息(分頁)
    @GetMapping("/history")
    public ResponseEntity<List<Message>> getPrivateChatHistory(
        @RequestParam String user1, 
        @RequestParam String user2,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        List<Message> messages = messageService.getPrivateChatHistory(user1, user2, page, size);
        return ResponseEntity.ok(messages);
    }

    // 查詢單一訊息
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Message>> getMessageById(@PathVariable String id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    // 查詢用戶的聊天列表（含未讀數）
    @GetMapping("/chat-list/{receiverId}")
    public ResponseEntity<List<ConversationDTO>> getMessageList(@PathVariable String receiverId) {
        return ResponseEntity.ok(messageService.getMessageList(receiverId));
    }

    // 私聊刪除訊息
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrivateMessage(@PathVariable String id) {
        Optional<Message> messageOptional = messageService.getMessageById(id);
        messageOptional.ifPresent(message -> {
            messageService.deleteMessage(id);

            // 通知 sender 和 receiver
            messagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/delete", id);
            messagingTemplate.convertAndSendToUser(message.getReceiverId(), "/queue/delete", id);
        });

        return ResponseEntity.noContent().build();
    }

    // 取得用戶未讀訊息數量
    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable String senderId, @PathVariable String receiverId) {
        int count = unreadMessageService.getUnreadCount(senderId, receiverId);
        return ResponseEntity.ok(count);
    }

    // 私聊標記訊息為已讀
    @PostMapping("/read/{id}")
    public ResponseEntity<Void> markPrivateMessageAsRead(@PathVariable String id) {
        Optional<Message> messageOptional = messageService.getMessageById(id);
        messageOptional.ifPresent(message -> {
            messageService.markMessageAsRead(id);

            // 通知 sender
            messagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/read", id);
        });

        return ResponseEntity.noContent().build();
    }
}
