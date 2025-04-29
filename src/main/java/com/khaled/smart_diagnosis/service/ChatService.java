package com.khaled.smart_diagnosis.service;

import com.khaled.smart_diagnosis.DTO.StartChatRequest;
import com.khaled.smart_diagnosis.model.ChatMessage;
import com.khaled.smart_diagnosis.model.ChatSession;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.repository.ChatMessageRepository;
import com.khaled.smart_diagnosis.repository.ChatSessionRepository;
import com.khaled.smart_diagnosis.repository.PatientRepository;
import com.khaled.smart_diagnosis.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PatientRepository patientRepository;
    private final JWTUtil jwtUtil;

    public ChatSession startNewSession(String authHeader, StartChatRequest request) {
        Patient patient = extractPatientFromAuthHeader(authHeader);

        log.info("Starting new chat session for patient: {}", patient.getEmail());

        String firstMessage = request.getFirstMessage();
        if (firstMessage == null || firstMessage.trim().isEmpty()) {
            firstMessage = "Welcome! How can I assist you today?";
        }

        String title = generateSessionTitle(firstMessage);

        ChatSession session = new ChatSession();
        session.setPatient(patient);
        session.setTitle(title);

        ChatSession savedSession = chatSessionRepository.save(session);
        log.info("New session started with ID: {}", savedSession.getId());

        ChatMessage firstChatMessage = new ChatMessage();
        firstChatMessage.setSession(savedSession);
        firstChatMessage.setSender("PATIENT");
        firstChatMessage.setMessage(firstMessage);

        chatMessageRepository.save(firstChatMessage);
        log.info("First message from patient saved for session ID: {}", savedSession.getId());

        return savedSession;
    }

    public List<ChatSession> getPatientSessions(String authHeader) {
        Patient patient = extractPatientFromAuthHeader(authHeader);

        log.info("Fetching chat sessions for patient: {}", patient.getEmail());

        return chatSessionRepository.findByPatientId(patient.getId());
    }

    public void saveBotResponse(Long sessionId, String responseMessage) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found for ID: " + sessionId));

        log.info("Saving bot response for session ID: {}", sessionId);

        // لو الرد يحتوي على تشخيص ومفيش عنوان مناسب اتحدث العنوان
        if (responseMessage.contains("تشخيص")) {
            updateSessionTitle(sessionId, "تشخيص حالة مرضية");
        }

        ChatMessage botMessage = new ChatMessage();
        botMessage.setSession(session);
        botMessage.setSender("BOT");
        botMessage.setMessage(responseMessage);

        chatMessageRepository.save(botMessage);
        log.info("Bot response saved for session ID: {}", sessionId);
    }

    public void deleteSession(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found for ID: " + sessionId));
        chatSessionRepository.delete(session);
    }



    private Patient extractPatientFromAuthHeader(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            log.error("Invalid or expired token");
            throw new RuntimeException("Invalid or expired token");
        }

        String email = jwtUtil.extractUsername(token);

        log.info("Extracting patient by email: {}", email);

        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found with email: " + email));
    }

    public void updateSessionTitle(Long sessionId, String newTitle) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found for ID: " + sessionId));

        session.setTitle(newTitle);
        chatSessionRepository.save(session);

        log.info("Session title updated for session ID: {}", sessionId);
    }

    public List<ChatMessage> getSessionMessages(Long sessionId) {
        return chatMessageRepository.findBySessionId(sessionId);
    }

    /**
     * Generates a smart session title based on the first message.
     *
     * @param message The patient's first message
     * @return A smart title for the chat session
     */
    private String generateSessionTitle(String message) {
        String msg = message.toLowerCase();

        if (msg.contains("برد") || msg.contains("زكام") || msg.contains("انفلونزا")) {
            return "تشخيص دور برد";
        } else if (msg.contains("صداع")) {
            return "استشارة حول الصداع";
        } else if (msg.contains("معدة") || msg.contains("غثيان") || msg.contains("ترجيع")) {
            return "مشاكل في المعدة";
        } else if (msg.contains("سعال") || msg.contains("كحة") || msg.contains("تنفس")) {
            return "مشاكل بالجهاز التنفسي";
        } else if (msg.contains("حمى") || msg.contains("حرارة") || msg.contains("سخونية")) {
            return "ارتفاع درجة الحرارة";
        } else if (msg.contains("ألم") || msg.contains("وجع") || msg.contains("مغص")) {
            return "استشارة حول الألم";
        } else {
            return message.length() > 30 ? message.substring(0, 30) + "..." : message;
        }
    }
}
