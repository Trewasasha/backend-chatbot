package com.example.chatbot_backend.service;

import com.example.chatbot_backend.config.TelegramBotConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class TelegramService extends TelegramLongPollingBot implements InitializingBean {
    private final TelegramBotConfig config;

    public TelegramService(TelegramBotConfig config) {
        super(config.getToken());
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("Telegram Bot Successfully Initialized:");
        System.out.println("Bot Name: " + config.getName());
        System.out.println("Chat ID: " + config.getChatId());
        System.out.println("Notifications Enabled: " + config.isNotificationEnabled());
    }

    public void sendNotification(String message) throws TelegramApiException {
        if (!config.isNotificationEnabled()) {
            System.out.println("Notification skipped (disabled in config)");
            return;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(config.getChatId());
        sendMessage.setText(message);
        sendMessage.enableMarkdown(true);

        try {
            execute(sendMessage);
            System.out.println("Message sent to Telegram successfully");
        } catch (TelegramApiException e) {
            System.err.println("Failed to send Telegram message: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onUpdateReceived(Update update) {}

    @Override
    public String getBotUsername() {
        return config.getName();
    }
}