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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatCacheService chatCacheService;
    private final ObjectMapper objectMapper;

    // 儲存訊息（存 MongoDB + Redis）
    public Message saveMessage(Message message) {
        validateUsers(message.getSenderId(), message.getReceiverId()); // 確認發送者與接收者存在
        Message savedMessage = messageRepository.save(message);

        // 快取到 Redis
        chatCacheService.cacheMessage(savedMessage);

        // 新增聊天對象快取更新（雙方）
        chatCacheService.chatListCache(message.getSenderId(), message.getReceiverId(), savedMessage.getId());
        chatCacheService.chatListCache(message.getReceiverId(), message.getSenderId(), savedMessage.getId());
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
    
        Set<String> seenIds = new HashSet<>();
        List<Message> result = new ArrayList<>();
    
        // Redis 查詢
        List<String> cachedMessagesJson = chatCacheService.getCachedMessages(tempMessage, page, size);
        for (String json : cachedMessagesJson) {
            try {
                Message msg = objectMapper.readValue(json, Message.class);
                if (seenIds.add(msg.getId())) {
                    result.add(msg);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing cached message", e);
            }
        }
    
        // 從 MongoDB 補充，如果 Redis 不足或有重複
        if (result.size() < size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            Page<Message> dbMessages = messageRepository.findChatHistory(user1, user2, pageable);
            for (Message msg : dbMessages.getContent()) {
                if (seenIds.add(msg.getId())) {
                    result.add(msg);
                }
            }
        }

        return result;
    }
    

    // 查詢聊天列表
    public List<String> getChatList(String userId) {
        List<String> redisChatList = chatCacheService.getChatListFromRedis(userId);
        if (!redisChatList.isEmpty()) return redisChatList;

        List<Message> messages = messageRepository.findBySenderIdOrReceiverId(userId, userId);
        Set<String> chatPartners = new HashSet<>();
        for (Message message : messages) {
            if (!message.getSenderId().equals(userId)) {
                chatPartners.add(message.getSenderId());
            } else if (!message.getReceiverId().equals(userId)) {
                chatPartners.add(message.getReceiverId());
            }
        }

        return new ArrayList<>(chatPartners);
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

            // 刪除 Redis 快取
            chatCacheService.deleteMessageFromCache(message);
        });
    }

}
