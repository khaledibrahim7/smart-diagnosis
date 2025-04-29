package com.khaled.smart_diagnosis.repository;

import com.khaled.smart_diagnosis.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByPatientId(Long patientId);
}