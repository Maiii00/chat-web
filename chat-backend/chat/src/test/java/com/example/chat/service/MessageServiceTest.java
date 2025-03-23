package com.example.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;

import org.mockito.InjectMocks;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void testSendMessage() {
        Message message = new Message("Alice", "Bob", "Hello!", LocalDateTime.now());
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        Message result = messageService.saveMessage("Alice", "Bob", "Hello!");

        assertNotNull(result);
        assertEquals("Alice", result.getSender());
        assertEquals("Bob", result.getReceiver());
        assertEquals("Hello!", result.getContent());

        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testGetMessages() {
        Message msg1 = new Message("Alice", "Bob", "Hi!", LocalDateTime.now());
        Message msg2 = new Message("Alice", "Bob", "How are you?", LocalDateTime.now());

        when(messageRepository.findBySenderAndReceiver("Alice", "Bob"))
            .thenReturn(List.of(msg1, msg2));

        List<Message> messages = messageService.getMessages("Alice", "Bob");

        assertEquals(2, messages.size());
        verify(messageRepository, times(1)).findBySenderAndReceiver("Alice", "Bob");
    }

    @Test
    void testGetMessagesById() {
        Message message = new Message("Alice", "Bob", "Hey!", LocalDateTime.now());
        when(messageRepository.findById("123")).thenReturn(Optional.of(message));

        Optional<Message> result = messageService.getMessageById("123");

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getSender());
    }

    @Test
    void testDeleteMessage() {
        doNothing().when(messageRepository).deleteById("123");

        messageService.deleteMessage("123");

        verify(messageRepository, times(1)).deleteById("123");
    }
}
