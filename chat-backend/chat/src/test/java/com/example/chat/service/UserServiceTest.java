package com.example.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.chat.config.jwt.JwtUtil;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testUser");
        // user.setEmail("test@example.com");
        user.setPassword("password");
    }

    @Test
    void testRegister() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        String result = userService.register(user);

        assertEquals("User registered successfully", result); // 當用戶名未被使用時，應該註冊成功
        verify(userRepository, times(1)).save(user); // 確保 userRepository.save() 只被呼叫一次
    }

    @Test
    void testUsernameTaken() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        String result = userService.register(user);

        assertEquals("Username already taken", result); // 當用戶名已被使用時，應該返回錯誤訊息
        verify(userRepository, never()).save(any(User.class)); // 確保用戶不會被儲存
    }

    @Test
    void testLogin() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        //when(jwtUtil.generateAccessToken("testUser")).thenReturn("mockAccessToken");
        //when(jwtUtil.generateRefreshToken("testUser")).thenReturn("mockRefreshToken");

        Map<String, String> tokens = userService.login(user);

        assertEquals("mockAccessToken", tokens.get("accessToken")); // 應該返回正確的 accessToken
        assertEquals("mockRefreshToken", tokens.get("refreshToken")); // 應該返回正確的 refreshToken
    }

    @Test
    void testUserNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        // Act & Assert（驗證例外拋出）
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(user);
        });

        assertEquals("User not found", exception.getMessage()); // 當找不到用戶時，應該拋出 'User not found' 錯誤
    }

    @Test
    void testInvalidPassword() {
        User storedUser = new User();
        storedUser.setUsername("testUser");
        // storedUser.setEmail("test@example.com");
        storedUser.setPassword("wrongPassword");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(storedUser));

        // Act & Assert（驗證例外拋出）
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(user);
        });

        assertEquals("Invalid credentials", exception.getMessage()); // 當密碼錯誤時，應該拋出 'Invalid credentials' 錯誤
    }
}
