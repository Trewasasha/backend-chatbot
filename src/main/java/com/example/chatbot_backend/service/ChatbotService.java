package com.example.chatbot_backend.service;

import com.example.chatbot_backend.model.UserContactSession;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class ChatbotService {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Регулярные выражения для команд
    private static final Pattern PRICING_PATTERN = 
        Pattern.compile("(цена|стоимость|прайс|сколько стоит|цен[ыу]|price|cost|pricing)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern GREETING_PATTERN = 
        Pattern.compile("(привет|здравствуй|здравствуйте|добр(ый|ого)\\s(день|вечер|утро|ночи)|hello|hi|хай|здрасьте|здаров|прив|здорово|доброго времени суток|здоро?во)",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern CONTACT_REQUEST_PATTERN = 
        Pattern.compile("(свяж[итесь]*|контакт|перезвон[ите]*|связаться|связь с владельцем|contact|связ[ьи]|напиши|позвони)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SPRING_PATTERN = 
        Pattern.compile("(spring|спринг|spring[\\s-]*boot|спринг[\\s-]*бут|фреймворк)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern POSTGRES_PATTERN = 
        Pattern.compile("(postgres|postgre\\s*sql|постгрес|постгре|бд|база\\s*данных|database)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern DOCKER_PATTERN = 
        Pattern.compile("(docker|докер|контейнер|container)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SERVICES_PATTERN = 
        Pattern.compile("(услуг[иа]|сервис|service|разработк[ау]|сделать|создать|разработ[ау]|создани[ею])", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern COLLABORATION_PATTERN = 
        Pattern.compile("(сотрудничеств[оа]|начать работу|проект|заказ|collab|заказать|сотрудничать|работать вместе)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern THANKS_PATTERN = 
        Pattern.compile("(спасибо|благодарю|thanks|thank you|мерси|от души)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern BYE_PATTERN = 
        Pattern.compile("(пока|до свидания|до встречи|всего доброго|goodbye|bye|чао|до завтра)", 
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final TelegramService telegramService;
    private final Map<String, UserContactSession> contactSessions = new ConcurrentHashMap<>();

    public ChatbotService(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    public String processMessage(String sessionId, String message) {
        System.out.println("Processing message - Session: " + sessionId + ", Text: " + message);
        
        // 1. Проверка активной сессии
        if (sessionId != null && contactSessions.containsKey(sessionId)) {
            return processContactSession(sessionId, message.trim());
        }

        // 2. Нормализация сообщения
        String normalizedMsg = normalizeMessage(message);
        
        // 3. Обработка команды связи
        if (CONTACT_REQUEST_PATTERN.matcher(normalizedMsg).find()) {
            contactSessions.put(sessionId, new UserContactSession());
            return "Пожалуйста, введите ваш email для связи:";
        }

        // 4. Обработка коротких сообщений
        if (normalizedMsg.length() < 2) {
            return getDefaultResponse();
        }

        // 5. Обработка регулярных команд
        return handleRegularCommands(normalizedMsg);
    }

    private String processContactSession(String sessionId, String message) {
        UserContactSession session = contactSessions.get(sessionId);
        if (session == null) {
            contactSessions.remove(sessionId);
            return "Сессия устарела. Пожалуйста, начните заново.";
        }

        if (session.isAwaitingEmail()) {
            if (!isValidEmail(message)) {
                return "Некорректный email. Пример: name@example.com\nПожалуйста, введите email:";
            }
            session.setEmail(message);
            session.setAwaitingEmail(false);
            session.setAwaitingMessage(true);
            return "Теперь введите ваше сообщение:";
        }

        if (session.isAwaitingMessage()) {
            if (message.trim().isEmpty()) {
                return "Сообщение не может быть пустым. Пожалуйста, введите текст:";
            }
            
            String result = sendContactRequest(session, message);
            contactSessions.remove(sessionId);
            return result;
        }

        contactSessions.remove(sessionId);
        return "Ошибка сессии. Пожалуйста, начните заново.";
    }

    private String sendContactRequest(UserContactSession session, String message) {
        try {
            String telegramMsg = String.format(
                "📩 Новый контактный запрос\n\n" +
                "📧 Email: %s\n\n" +
                "✉️ Сообщение:\n%s\n\n" +
                "🕒 Время: %s",
                session.getEmail(),
                message,
                java.time.LocalDateTime.now()
            );
            
            telegramService.sendNotification(telegramMsg);
            return "✅ Ваше сообщение отправлено! Мы свяжемся с вами в ближайшее время.";
        } catch (TelegramApiException e) {
            System.err.println("Telegram send error: " + e.getMessage());
            return "❌ Ошибка при отправке. Пожалуйста, попробуйте позже.";
        }
    }

    private String normalizeMessage(String message) {
        return message.trim()
                     .replaceAll("[!?,.]", "")
                     .replaceAll("\\s+", " ")
                     .toLowerCase();
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private String handleRegularCommands(String normalizedMsg) {
        if (GREETING_PATTERN.matcher(normalizedMsg).find()) {
            return "Здравствуйте! Чем могу помочь?";
        }
        
        if (THANKS_PATTERN.matcher(normalizedMsg).find()) {
            return "Пожалуйста! Обращайтесь, если будут вопросы.";
        }
        
        if (BYE_PATTERN.matcher(normalizedMsg).find()) {
            return "До свидания! Хорошего дня!";
        }
        
        if (PRICING_PATTERN.matcher(normalizedMsg).find()) {
            return getPricingInformation();
        }
        
        if (SPRING_PATTERN.matcher(normalizedMsg).find()) {
            return "Да, Spring Boot - мой основной фреймворк для разработки backend-приложений.";
        }
        
        if (POSTGRES_PATTERN.matcher(normalizedMsg).find()) {
            return "Имею коммерческий опыт работы с PostgreSQL, включая оптимизацию запросов и настройку репликации.";
        }
        
        if (DOCKER_PATTERN.matcher(normalizedMsg).find()) {
            return "Активно использую Docker для развертывания приложений, включая Docker Compose для оркестрации.";
        }
        
        if (SERVICES_PATTERN.matcher(normalizedMsg).find()) {
            return "Мои основные услуги:\n" +
                   "- Разработка REST API на Spring Boot\n" +
                   "- Создание микросервисной архитектуры\n" +
                   "- Интеграция с различными базами данных\n" +
                   "- Docker-развертывание и настройка CI/CD";
        }
        
        if (COLLABORATION_PATTERN.matcher(normalizedMsg).find()) {
            return "Для обсуждения проекта или сотрудничества напишите 'Свяжите меня с владельцем сайта' или оставьте свои контакты.";
        }
        
        return getDefaultResponse();
    }

    private String getPricingInformation() {
        return "💰 Стоимость услуг:\n\n" +
               "• Разработка REST API - от 25 000 руб.\n" +
               "• Микросервисная архитектура - от 50 000 руб.\n" +
               "• Интеграция с базами данных - от 15 000 руб.\n" +
               "• Docker-развертывание - от 10 000 руб.\n\n" +
               "Точная стоимость рассчитывается индивидуально для каждого проекта.";
    }

    private String getDefaultResponse() {
        return "Я не совсем понял ваш запрос. Вот что я могу:\n\n" +
               "📌 Узнать цены: напишите 'цены' или 'стоимость'\n" +
               "📌 О технологиях: спросите про Spring, PostgreSQL или Docker\n" +
               "📌 Связаться: напишите 'свяжитесь со мной'\n" +
               "📌 Сотрудничество: 'хочу заказать проект' или 'сотрудничество'";
    }

    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}