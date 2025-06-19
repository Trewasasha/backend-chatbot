package com.example.chatbot_backend.dto;

public class ChatResponse {
    private String response;
    private String sessionId;

    public ChatResponse(String response, String sessionId) {
        this.response = response;
        this.sessionId = sessionId;
    }

    // Геттеры
    public String getResponse() {
        return response;
    }

    public String getSessionId() {
        return sessionId;
    }
}