package com.example.chat.service;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UnreadMessageService unreadMessageService;
    private final ChatCacheService chatCacheService;

    // 儲存訊息（存 MongoDB + Redis）
    public Message saveMessage(Message message) {
        validateUsers(message.getSenderId(), message.getReceiverId()); // 確認發送者與接收者存在
        Message savedMessage = messageRepository.save(message);
        unreadMessageService.incrementUnreadCount(message.getReceiverId()); // 增加未讀訊息數

        // 快取到 Redis
        chatCacheService.cacheMessage(savedMessage);
        return savedMessage;
    }

    // 確認user存在
    private void validateUsers(String senderId, String receiverId) {
        if (!userRepository.existsById(senderId) || !userRepository.existsById(receiverId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender or receiver not found");
        }
    }

    // 查詢私聊用戶之間聊天紀錄
    public List<Message> getPrivateChatHistory(String user1, String user2, int page, int size) {
        Message tempMessage = new Message();
        tempMessage.setSenderId(user1);
        tempMessage.setReceiverId(user2);

        // 先從 Redis 取得快取訊息
        List<String> cachedMessagesJson = chatCacheService.getCachedMessages(tempMessage, page, size);
        if (!cachedMessagesJson.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return cachedMessagesJson.stream().map(json -> {
                try {
                    return objectMapper.readValue(json, Message.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error deserializing message from Redis", e);
                }
            }).toList();
        }

        // 否則從 MongoDB 查詢
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Message> dbMessages = messageRepository.findChatHistory(user1, user2, pageable);
        return dbMessages.getContent();
    }

    // 查詢單一紀錄
    public Optional<Message> getMessageById(String id) {
        return messageRepository.findById(id);
    }

    // 刪除訊息（刪 MongoDB + Redis）
    public void deleteMessage(String id) {
        Optional<Message> messageOptional = messageRepository.findById(id);
        messageOptional.ifPresent(message -> {
            messageRepository.deleteById(id);
            unreadMessageService.clearUnreadCount(message.getReceiverId());

            // 刪除 Redis 快取
            chatCacheService.clearChatCache(message);
        });
    }

    // 標記訊息為已讀
    public void markMessageAsRead(String id) {
        Optional<Message> messageOptional = messageRepository.findById(id);
        messageOptional.ifPresent(message -> {
            message.setRead(true);
            messageRepository.save(message);
            unreadMessageService.clearUnreadCount(message.getReceiverId());
        });
    }

}
