package com.example.chat.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.chat.model.Message;
import com.example.chat.service.MessageService;

@WebMvcTest(MessageController.class)
class MessageControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void testSendMessage() throws Exception {
        Message message = new Message("Alice", "Bob", "Hi!", LocalDateTime.now());
        when(messageService.saveMessage(any(), any(), any())).thenReturn(message);

        // 發送 POST 請求，並驗證回應
        mockMvc.perform(post("/api/messages")
                .param("sender", "Alice")
                .param("receiver", "Bob")
                .param("content", "Hi!"))
                .andExpect(status().isOk());
            
    }

    @Test
    void testGetMessage() throws Exception {
        Message msg1 = new Message("Alice", "Bob", "Hi!", LocalDateTime.now());
        Message msg2 = new Message("Alice", "Bob", "How are you?", LocalDateTime.now());
        when(messageService.getMessages("Alice", "Bob")).thenReturn(List.of(msg1, msg2));

        // 發送 GET 請求，並驗證回應
        mockMvc.perform(get("/api/messages")
                .param("sender", "Alice")
                .param("receiver", "Bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));    
    }

    @Test
    void testGetMessagesById() throws Exception {
        Message message = new Message("Alice", "Bob", "Hi!", LocalDateTime.now());
        when(messageService.getMessageById("123")).thenReturn(Optional.of(message));

        // 發送 GET 請求，抓 id 123
        mockMvc.perform(get("/api/messages/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sender").value("Alice"))
                .andExpect(jsonPath("$.receiver").value("Bob"))
                .andExpect(jsonPath("$.content").value("Hi!"));
    }

    @Test
    void testDeleteMessage() throws Exception{
        mockMvc.perform(delete("/api/messages/123"))
                .andExpect(status().isOk());

        verify(messageService, times(1)).deleteMessage("123");
    }
}
