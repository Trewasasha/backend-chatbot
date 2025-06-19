package com.example.chatbot_backend.controller;

import com.example.chatbot_backend.dto.ChatResponse;
import com.example.chatbot_backend.model.ChatRequest;
import com.example.chatbot_backend.service.ChatbotService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://185.135.80.107", "http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class ChatbotController {
    
    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ChatResponse handleMessage(
            @RequestBody ChatRequest request,
            @RequestHeader(name = "Session-Id", required = false) String sessionId) {
        
        System.out.println("Incoming request - SessionId: " + sessionId + ", Message: " + request.getMessage());
        
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = chatbotService.generateSessionId();
            System.out.println("Generated new SessionId: " + sessionId);
        }
        
        String response = chatbotService.processMessage(sessionId, request.getMessage());
        return new ChatResponse(response, sessionId);
    }

    @GetMapping("/session")
    public ChatResponse generateNewSession() {
        String newSessionId = chatbotService.generateSessionId();
        return new ChatResponse("Новая сессия создана", newSessionId);
    }
}