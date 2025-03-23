package com.example.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.example.chat.model.Message;

@DataMongoTest
class MessageRepositoryTest {
    
    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    @SuppressWarnings("unused")
    void cleanDatabaseBeforeTest() {
        messageRepository.deleteAll();
    }
    
    @Test
    void testSaveAndFindMessage() {
        Message message = new Message("Alice", "Bob", "Hello!", LocalDateTime.now());
        messageRepository.save(message);

        Optional<Message> found = messageRepository.findById(message.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getSender());
    }

    @Test
    void testFindBySenderAndReceiver() {
        Message msg1 = new Message("Alice", "Bob", "Hi!", LocalDateTime.now());
        Message msg2 = new Message("Alice", "Bob", "How are you?", LocalDateTime.now());
        messageRepository.save(msg1);
        messageRepository.save(msg2);

        List<Message> messages = messageRepository.findBySenderAndReceiver("Alice", "Bob");
        assertEquals(2, messages.size());
    }

    @AfterEach
    @SuppressWarnings("unused")
    void cleanDatabaseAfterTest() {
        messageRepository.deleteAll();
    }
}
