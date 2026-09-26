package com.example.springai.controller;

import com.example.springai.dto.ChatRequest;
import com.example.springai.dto.ChatResponse;
import com.example.springai.dto.CustomerInsight;
import com.example.springai.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * Standard Gemini call via Spring AI 2.0 ChatClient
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiAssistantService.askGemini(request.getMessage(), request.getSystemPrompt());
        return ResponseEntity.ok(response);
    }

    /**
     * Agentic Tool Calling with PostgreSQL:
     * Gemini dynamically queries PostgreSQL tools to answer customer questions
     */
    @PostMapping("/chat-with-db")
    public ResponseEntity<ChatResponse> chatWithDbTools(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiAssistantService.chatWithPostgresTools(request.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * Spring AI 2.0 Structured Output:
     * Reads customer from PostgreSQL and returns strongly typed JSON schema insight
     */
    @GetMapping("/insights/{customerId}")
    public ResponseEntity<CustomerInsight> getCustomerInsight(@PathVariable Long customerId) {
        CustomerInsight insight = aiAssistantService.generateCustomerInsight(customerId);
        return ResponseEntity.ok(insight);
    }
}
