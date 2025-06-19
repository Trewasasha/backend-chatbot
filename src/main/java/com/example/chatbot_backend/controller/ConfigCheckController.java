package com.example.chatbot_backend.controller;

import com.example.chatbot_backend.config.TelegramBotConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigCheckController {
    
    private final TelegramBotConfig config;

    public ConfigCheckController(TelegramBotConfig config) {
        this.config = config;
    }

    @GetMapping
    public String checkConfig() {
        return String.format(
            "Telegram Config:\n" +
            "Token: %s\n" +
            "Chat ID: %s\n" +
            "Bot Name: %s\n" +
            "Notifications Enabled: %s",
            config.getToken() != null ? "***" + config.getToken().substring(config.getToken().length() - 4) : "null",
            config.getChatId(),
            config.getName(),
            config.isNotificationEnabled()
        );
    }
}