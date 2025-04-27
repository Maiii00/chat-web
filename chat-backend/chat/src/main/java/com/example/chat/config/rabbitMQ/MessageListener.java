package com.example.chat.config.rabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.chat.model.Message;
import com.example.chat.service.MessageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void receiveMessage(Message message) {
        Message saved = messageService.saveMessage(message);
        messagingTemplate.convertAndSendToUser(
          message.getReceiverId(), "/queue/messages", saved  
        );

        messagingTemplate.convertAndSendToUser(
          message.getSenderId(), "/queue/messages", saved
        );
    }
}
