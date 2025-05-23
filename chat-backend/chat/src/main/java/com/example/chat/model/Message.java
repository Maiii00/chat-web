package com.example.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "messages")
@Data
@NoArgsConstructor
public class Message {
    @Id
    private String id;

    @Indexed
    private String senderId;

    @Indexed
    private String receiverId;

    private String content;

    public Message(String senderId, String receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }
}
