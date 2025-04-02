package com.example.chat.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.example.chat.model.User;

@DataMongoTest
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUpUser() {
        userRepository.deleteAll();
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setOnline(false);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("testUser");
        
        assertTrue(foundUser.isPresent());
        assertEquals("testUser", foundUser.get().getUsername());
        assertEquals("password", foundUser.get().getPassword());
        assertFalse(foundUser.get().isOnline());
    }

    @Test
    void testUserNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonExistentUser");

        assertFalse(foundUser.isPresent());
    }
}
