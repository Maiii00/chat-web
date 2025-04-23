package com.example.chat.controller;

import com.example.chat.model.Message;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.MessageService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final UserRepository userRepository;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket 私聊訊息處理
    @MessageMapping("/private") // 客戶端發送到 "/app/private"
    public void sendToUser(@Payload Message message) {
        Message saved = messageService.saveMessage(message); // 儲存 MongoDB + Redis + 未讀計數
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId(), "/queue/messages", saved
        );
        messagingTemplate.convertAndSendToUser(
            message.getSenderId(), "/queue/messages", saved
        );
    }

    // 送出訊息
    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("Principal name: " + principal.getName());
        String username = principal.getName();
        String senderId = userRepository.findByUsername(username)
                                        .map(user -> user.getId())
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
        message.setSenderId(senderId);
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

    // 查詢用戶的聊天列表
    @GetMapping("/chat-list")
    public ResponseEntity<List<String>> getMessageList(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String username = principal.getName();
        String userId = userRepository.findByUsername(username)
                            .map(user -> user.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
        List<String> chatList = messageService.getChatList(userId);
        return ResponseEntity.ok(chatList);
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
}
