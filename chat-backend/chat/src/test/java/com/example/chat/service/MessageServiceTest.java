package com.example.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;

import org.mockito.InjectMocks;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatCacheService chatCacheService;

    @InjectMocks
    private MessageService messageService;

    // 測試儲存訊息
    @Test
    void testSendMessage() {
        Message message = new Message("Alice", "Bob", "Hello!");

        when(userRepository.existsById("Alice")).thenReturn(true);
        when(userRepository.existsById("Bob")).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        Message result = messageService.saveMessage(message);

        assertNotNull(result);
        assertEquals("Alice", result.getSenderId());
        assertEquals("Bob", result.getReceiverId());
        assertEquals("Hello!", result.getContent());

        verify(userRepository, times(1)).existsById("Alice");
        verify(userRepository, times(1)).existsById("Bob");
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(chatCacheService, times(1)).cacheMessage(any(Message.class));  // 確認訊息有快取到 Redis
    }

    // 測試查詢歷史訊息
    @Test
    void testGetHistory() {
        Message msg1 = new Message("Alice", "Bob", "Hi!");
        Message msg2 = new Message("Alice", "Bob", "How are you?");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("timestamp").descending());
        Page<Message> messagePage = new PageImpl<>(List.of(msg1, msg2), pageable, 2);

        when(messageRepository.findChatHistory(eq("Alice"), eq("Bob"), eq(pageable)))
            .thenReturn(messagePage);

        // List<String> messages = messageService.getPrivateChatHistory("Alice", "Bob", 0, 10);

        // assertEquals(2, messages.size());
        // assertEquals("Alice", messages.get(0).getSenderId());
        // assertEquals("Bob", messages.get(0).getReceiverId());

        verify(messageRepository, times(1)).findChatHistory("Alice", "Bob", pageable);
    }

    // 測試查詢單一訊息
    @Test
    void testGetMessagesById() {
        Message message = new Message("Alice", "Bob", "Hey!");
        message.setId("123");
        when(messageRepository.findById("123")).thenReturn(Optional.of(message));

        Optional<Message> result = messageService.getMessageById("123");

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getSenderId());

        verify(messageRepository, times(1)).findById("123");
    }
}
