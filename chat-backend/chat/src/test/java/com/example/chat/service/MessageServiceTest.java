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
    private UnreadMessageService unreadMessageService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void testSendMessage() {
        Message message = new Message("Alice", "Bob", "Hello!", false);

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
        verify(unreadMessageService, times(1)).incrementUnreadCount(eq("Bob"));
    }

    @Test
    void testGetHistory() {
        Message msg1 = new Message("Alice", "Bob", "Hi!", false);
        Message msg2 = new Message("Alice", "Bob", "How are you?", false);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messagePage = new PageImpl<>(List.of(msg1, msg2), pageable, 2);

        when(messageRepository.findChatHistory(eq("Alice"), eq("Bob"), eq(PageRequest.of(0, 10))))
            .thenReturn(messagePage);

        Page<Message> messages = messageService.getChatHistory("Alice", "Bob", pageable);

        assertEquals(2, messages.getTotalElements());
        assertEquals(1, messages.getTotalPages());
        assertEquals("Alice", messages.getContent().get(0).getSenderId());

        verify(messageRepository, times(1)).findChatHistory("Alice", "Bob", pageable);
    }

    @Test
    void testGetMessagesById() {
        Message message = new Message("Alice", "Bob", "Hey!", false);
        message.setId("123");
        when(messageRepository.findById("123")).thenReturn(Optional.of(message));

        Optional<Message> result = messageService.getMessageById("123");

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getSenderId());

        verify(messageRepository, times(1)).findById("123");
    }

    @Test
    void testDeleteMessage() {
        Message message = new Message("Alice", "Bob", "Hi!", false);
        when(messageRepository.findById("123")).thenReturn(Optional.of(message));

        // 模擬 deleteById 不執行任何動作
        doNothing().when(messageRepository).deleteById("123");
        messageService.deleteMessage("123");
        
        verify(messageRepository, times(1)).deleteById("123");
        verify(unreadMessageService, times(1)).clearUnreadCount("Bob");
    }

    @Test
    void testMarkMessageAsRead() {
        Message message = new Message("Alice", "Bob", "Read this!", false);
        message.setId("456");

        when(messageRepository.findById("456")).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        messageService.markMessageAsRead("456");

        assertTrue(message.isRead());

        verify(messageRepository, times(1)).findById("456");
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(unreadMessageService, times(1)).clearUnreadCount(eq("Bob"));
    }
}
