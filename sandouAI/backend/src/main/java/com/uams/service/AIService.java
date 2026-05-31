package com.uams.service;

import org.springframework.stereotype.Service;

@Service
public class AIService {
    public String chat(String prompt) {
        return "This is a mock AI response to: " + prompt;
    }
}