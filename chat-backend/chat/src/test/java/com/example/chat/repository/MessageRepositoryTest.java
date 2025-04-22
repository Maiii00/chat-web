package com.example.chat.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.chat.model.Message;

@DataMongoTest
class MessageRepositoryTest {
    
    @Autowired
    private MessageRepository messageRepository;

    private Message msg1, msg2;

    @BeforeEach
    void setUpMessages() {
        messageRepository.deleteAll();

        msg1 = new Message("Alice", "Bob", "Hi!");
        msg2 = new Message("Alice", "Bob", "Hello!");

        messageRepository.saveAll(List.of(msg1, msg2));
    }
    
    @AfterEach
    void cleanDatabaseAfterTest() {
        messageRepository.deleteAll();
    }

    @Test
    void saveAndRetrieveMessageTest() {
        Message msg = new Message("Alice", "Bob", "Hi!");
        Message savedMsg = messageRepository.save(msg);

        Optional<Message> retrievedMsg = messageRepository.findById(savedMsg.getId());

        assertTrue(retrievedMsg.isPresent());
        assertEquals("Hi!", retrievedMsg.get().getContent());
    }

    @Test
    void findChatHistoryTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> chatHistory = messageRepository.findChatHistory("Alice", "Bob", pageable);

        assertEquals(2, chatHistory.getContent().size());
        //檢查順序是否正確
        assertEquals("Hi!", chatHistory.getContent().get(0).getContent());
        assertEquals("Hello!", chatHistory.getContent().get(1).getContent());
    }

    @Test
    void deleteMessageTest() {
        messageRepository.deleteById(msg1.getId());

        Optional<Message> deletedMsg = messageRepository.findById(msg1.getId());

        assertFalse(deletedMsg.isPresent());
    }
}
