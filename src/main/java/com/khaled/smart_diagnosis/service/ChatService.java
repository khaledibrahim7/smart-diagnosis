package com.khaled.smart_diagnosis.service;

import com.khaled.smart_diagnosis.model.Message;
import com.khaled.smart_diagnosis.model.ChatSession;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.repository.MessageRepository;
import com.khaled.smart_diagnosis.repository.ChatSessionRepository;
import com.khaled.smart_diagnosis.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final MessageRepository messageRepository;
    private final PatientRepository patientRepository;

    private static final int MAX_CHATS_PER_PATIENT = 20;

    public ChatSession createNewChat(Long patientId, String title) {
        int chatCount = chatSessionRepository.countByPatientId(patientId);
        if (chatCount >= MAX_CHATS_PER_PATIENT) {
            throw new IllegalStateException("Maximum conversations is 20. Delete one before creating a new one.");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("The patient is not present"));

        ChatSession chat = new ChatSession();
        chat.setTitle(null);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setPatient(patient);

        return chatSessionRepository.save(chat);
    }

    public List<ChatSession> getPatientChats(Long patientId) {
        return chatSessionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<Message> getChatMessages(Long chatId) {
        return messageRepository.findByChatSessionIdOrderByTimestampAsc(chatId);
    }

    public void deleteChat(Long chatId, Long patientId) {
        ChatSession chat = chatSessionRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("The conversation does not exist"));

        if (!chat.getPatient().getId().equals(patientId)) {
            log.error("Attempt to delete an unauthorized conversation from the patient ID: {}", patientId);
            throw new SecurityException("You are not allowed to delete this conversation.");
        }

        chatSessionRepository.delete(chat);
        log.info("Conversation ID: {} has been successfully deleted.", chatId);
    }

    public Message addMessage(Long chatId, boolean fromPatient, String content) {
        ChatSession chat = chatSessionRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("The conversation does not exist"));

        if (fromPatient && chat.getTitle() == null) {
            chat.setTitle(content);
            chatSessionRepository.save(chat);
            log.info("Conversation title ID: {} updated to: {}", chatId, content);
        }

        Message message = new Message();
        message.setChatSession(chat);
        message.setFromPatient(fromPatient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        if (fromPatient) {
            log.info("The patient's message has been saved in the conversation. ID: {}", chatId);
        } else {
            log.info("Bot response saved in conversation ID: {}", chatId);
        }

        return savedMessage;
    }

}
