package com.example.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.chat.model.Message;

public interface MessageRepository extends MongoRepository<Message, String> {

    // MongoDB 查詢雙向聊天紀錄，並支援分頁
    @Query("{ '$or': [ " +
            "{ '$and': [ {'senderId': ?0}, {'receiverId': ?1} ] }, " +
            "{ '$and': [ {'senderId': ?1}, {'receiverId': ?0} ] } " +
          "] }")
    Page<Message> findChatHistory(
        String userId1, String userId2, Pageable pageable
    );
}
