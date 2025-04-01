package com.example.chat.controller;

import com.example.chat.model.Message;
import com.example.chat.service.MessageService;
import com.example.chat.service.UnreadMessageService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<Message>> getChatHistory(
        @RequestParam String user1, 
        @RequestParam String user2,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        Page<Message> messages = messageService.getChatHistory(user1, user2, PageRequest.of(page, size));
        return ResponseEntity.ok(messages);
    }

    // 查詢單一訊息
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Message>> getMessageById(@PathVariable String id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    // 刪除訊息
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String id) {
        messageService.deleteMessage(id);
        messagingTemplate.convertAndSend("/topic/delete", id);
        return ResponseEntity.noContent().build();
    }

    //取得用戶未讀訊息數量
    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable String userId) {
        int count = unreadMessageService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    

    // 標記訊息為已讀
    @PostMapping("/read/{id}")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable String id) {
        messageService.markMessageAsRead(id);
        messagingTemplate.convertAndSend("/topic/read", id);
        return ResponseEntity.noContent().build();
    }
}
