package com.example.chat.service;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message saveMessage(String sender, String receiver, String content) {
        Message message = new Message(sender, receiver, content, LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getMessages(String sender, String receiver) {
        return messageRepository.findBySenderAndReceiver(sender, receiver);
    }

    public Optional<Message> getMessageById(String id) {
        return messageRepository.findById(id);
    }

    public void deleteMessage(String id) {
        messageRepository.deleteById(id);
    }
}
