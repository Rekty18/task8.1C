package com.example.chatbot;

import java.util.List;
import java.util.Map;

public class ChatRequest {
    private String userMessage;
    private List<Map<String, String>> chatHistory;

    public ChatRequest(String userMessage, List<Map<String, String>> chatHistory) {
        this.userMessage = userMessage;
        this.chatHistory = chatHistory;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public List<Map<String, String>> getChatHistory() {
        return chatHistory;
    }
}
