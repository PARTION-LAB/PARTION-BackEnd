package com.partion.ai.dto;

import lombok.Getter;

@Getter
public class AiChatResponse {

    private final String answer;

    public AiChatResponse(String answer) {
        this.answer = answer;
    }
}
