package com.khaled.smart_diagnosis.controller;

import com.khaled.smart_diagnosis.DTO.ChatSessionDTO;
import com.khaled.smart_diagnosis.DTO.MessageDTO;
import com.khaled.smart_diagnosis.ResponseWrapper.ResponseWrapper;
import com.khaled.smart_diagnosis.model.Message;
import com.khaled.smart_diagnosis.model.ChatSession;
import com.khaled.smart_diagnosis.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ChatSessionDTO>>> getAllChats(@RequestParam Long patientId) {
        List<ChatSession> chats = chatService.getPatientChats(patientId);
        List<ChatSessionDTO> chatDTOs = chats.stream()
                .map(chat -> new ChatSessionDTO(chat.getId(), chat.getTitle(), chat.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseWrapper<>(chatDTOs, "Chats retrieved successfully", true));
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<ChatSessionDTO>> createChat(
            @RequestParam Long patientId,
            @RequestParam String title
    ) {
        ChatSession chat = chatService.createNewChat(patientId, title);
        ChatSessionDTO chatDTO = new ChatSessionDTO(chat.getId(), chat.getTitle(), chat.getCreatedAt());
        return ResponseEntity.ok(new ResponseWrapper<>(chatDTO, "Chat created successfully", true));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ResponseWrapper<List<MessageDTO>>> getMessages(@PathVariable Long chatId) {
        List<Message> messages = chatService.getChatMessages(chatId);
        List<MessageDTO> messageDTOs = messages.stream()
                .map(msg -> new MessageDTO(msg.getId(), msg.getContent(), msg.isFromPatient(), msg.getTimestamp()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ResponseWrapper<>(messageDTOs, "Messages retrieved successfully", true));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ResponseWrapper<MessageDTO>> addMessage(
            @PathVariable Long chatId,
            @RequestParam boolean fromPatient,
            @RequestParam String content
    ) {
        Message message = chatService.addMessage(chatId, fromPatient, content);
        MessageDTO messageDTO = new MessageDTO(message.getId(), message.getContent(), message.isFromPatient(), message.getTimestamp());
        return ResponseEntity.ok(new ResponseWrapper<>(messageDTO, "Message added successfully", true));
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<ResponseWrapper<Void>> deleteChat(
            @PathVariable Long chatId,
            @RequestParam Long patientId
    ) {
        chatService.deleteChat(chatId, patientId);
        return ResponseEntity.noContent().build();
    }
}