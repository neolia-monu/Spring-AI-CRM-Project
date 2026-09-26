package com.example.springai.controller;

import com.example.springai.dto.ChatRequest;
import com.example.springai.dto.ChatResponse;
import com.example.springai.dto.CustomerInsight;
import com.example.springai.service.AiAssistantService;
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
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Message cannot be empty", "error"));
        }
        ChatResponse response = aiAssistantService.askGemini(request.getMessage(), request.getSystemPrompt());
        return ResponseEntity.ok(response);
    }

    /**
     * Agentic Tool Calling with PostgreSQL:
     * Gemini dynamically queries PostgreSQL tools to answer customer questions
     */
    @PostMapping("/chat-with-db")
    public ResponseEntity<ChatResponse> chatWithDbTools(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Message cannot be empty", "error"));
        }
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponse> handleAiException(Exception ex) {
        ex.printStackTrace();
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String rootMsg = root.getMessage() != null ? root.getMessage() : ex.getMessage();
        if (rootMsg != null && (rootMsg.contains("API_KEY_INVALID") || rootMsg.contains("401") || rootMsg.contains("your_gemini_api_key_here"))) {
            return ResponseEntity.status(401).body(new ChatResponse(
                    "Gemini API authentication failed. Please configure a valid Google Gemini API Key via the 'SPRING_AI_GOOGLE_GENAI_API_KEY' environment variable. (Get one free at https://aistudio.google.com/apikey)",
                    "auth_error"
            ));
        }
        return ResponseEntity.status(500).body(new ChatResponse("AI processing error: " + rootMsg, "error"));
    }
}
