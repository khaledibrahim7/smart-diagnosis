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
            throw new IllegalStateException("الحد الأقصى لعدد المحادثات هو 20. احذف واحدة قبل إنشاء جديدة.");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("المريض مش موجود"));

        ChatSession chat = new ChatSession();
        chat.setTitle(title);
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
                .orElseThrow(() -> new RuntimeException("المحادثة غير موجودة"));

        if (!chat.getPatient().getId().equals(patientId)) {
            log.error("محاولة حذف محادثة غير مصرح بها من المريض ID: {}", patientId);
            throw new SecurityException("غير مصرح لك بحذف هذه المحادثة");
        }

        chatSessionRepository.delete(chat);
        log.info("تم حذف المحادثة ID: {} بنجاح", chatId);
    }

    public Message addMessage(Long chatId, boolean fromPatient, String content) {
        ChatSession chat = chatSessionRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("المحادثة مش موجودة"));

        Message message = new Message();
        message.setChatSession(chat);
        message.setFromPatient(fromPatient);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        if (fromPatient) {
            log.info("تم حفظ رسالة المريض في المحادثة ID: {}", chatId);
        } else {
            log.info("تم حفظ رد البوت في المحادثة ID: {}", chatId);
        }

        return savedMessage;
    }
}
