package com.example.chat.service;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UnreadMessageService unreadMessageService;

    // 儲存 message
    public Message saveMessage(Message message) {
        validateUsers(message.getSenderId(), message.getReceiverId());
        Message savedMessage = messageRepository.save(message);
        unreadMessageService.incrementUnreadCount(message.getReceiverId());
        return savedMessage;
    }

    // 確認user存在
    private void validateUsers(String senderId, String receiverId) {
        if (!userRepository.existsById(senderId) || !userRepository.existsById(receiverId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender or receiver not found");
        }
    }

    // 查詢用戶之間聊天紀錄
    public Page<Message> getChatHistory(String user1, String user2, Pageable pageable) {
        return messageRepository.findChatHistory(user1, user2, pageable);
    }

    // 查詢單一紀錄
    public Optional<Message> getMessageById(String id) {
        return messageRepository.findById(id);
    }

    // 刪除 message
    public void deleteMessage(String id) {
        Optional<Message> messageOptional = messageRepository.findById(id);
        messageOptional.ifPresent(message -> {
            messageRepository.deleteById(id);
            unreadMessageService.clearUnreadCount(message.getReceiverId());
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
