package com.khaled.smart_diagnosis.controller;

import com.khaled.smart_diagnosis.DTO.StartChatRequest;
import com.khaled.smart_diagnosis.model.ChatMessage;
import com.khaled.smart_diagnosis.model.ChatSession;
import com.khaled.smart_diagnosis.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/start")
    public ResponseEntity<?> startNewSession(@RequestHeader("Authorization") String authHeader, @RequestBody StartChatRequest request) {
        return new ResponseEntity<>(chatService.startNewSession(authHeader, request), HttpStatus.CREATED);
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getPatientSessions(@RequestHeader("Authorization") String authHeader) {
        return new ResponseEntity<>(chatService.getPatientSessions(authHeader), HttpStatus.OK);
    }

    @PostMapping("/save-bot-response/{sessionId}")
    public ResponseEntity<?> saveBotResponse(@PathVariable Long sessionId, @RequestBody String responseMessage) {
        chatService.saveBotResponse(sessionId, responseMessage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable Long sessionId) {
        chatService.deleteSession(sessionId);
        return ResponseEntity.ok("Session deleted successfully");
    }


    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<?> getSessionMessages(@PathVariable Long sessionId) {
        List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

}