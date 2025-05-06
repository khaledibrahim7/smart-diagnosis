package com.khaled.smart_diagnosis.repository;

import com.khaled.smart_diagnosis.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatSessionIdOrderByTimestampAsc(Long chatSessionId);


}