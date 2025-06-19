package com.example.chatbot_backend.model;


public class UserContactSession {
    
    private String email;
    private String message;
    private boolean awaitingEmail = true;
    private boolean awaitingMessage = false;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAwaitingEmail() {
        return awaitingEmail;
    }

    public void setAwaitingEmail(boolean awaitingEmail) {
        this.awaitingEmail = awaitingEmail;
    }

    public boolean isAwaitingMessage() {
        return awaitingMessage;
    }

    public void setAwaitingMessage(boolean awaitingMessage) {
        this.awaitingMessage = awaitingMessage;
    }
}