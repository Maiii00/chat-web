package com.example.chat.controller;

import com.example.chat.model.Message;
import com.example.chat.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // 送出訊息
    @PostMapping
    public Message sendMessage(@RequestParam String sender, @RequestParam String receiver, @RequestParam String content) {
        return messageService.saveMessage(sender, receiver, content);
    }

    // 查詢歷史訊息
    @GetMapping
    public List<Message> getMessages(@RequestParam String sender, @RequestParam String receiver) {
        return messageService.getMessages(sender, receiver);
    }

    // 查詢單一訊息
    @GetMapping("/{id}")
    public Optional<Message> getMessageById(@PathVariable String id) {
        return messageService.getMessageById(id);
    }

    // 刪除訊息
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable String id) {
        messageService.deleteMessage(id);
    }
}
