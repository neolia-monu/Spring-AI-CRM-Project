package com.example.springai.service;

import com.example.springai.dto.ChatResponse;
import com.example.springai.dto.CustomerInsight;
import com.example.springai.model.Customer;
import com.example.springai.repository.CustomerRepository;
import com.example.springai.tools.DatabaseCustomerTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AiAssistantService {

    private final ChatClient chatClient;
    private final DatabaseCustomerTools databaseCustomerTools;
    private final CustomerRepository customerRepository;

    @Value("${spring.ai.google.genai.chat.model:gemini-3.1-flash-lite}")
    private String configuredModel;

    public AiAssistantService(
            ChatClient.Builder chatClientBuilder,
            DatabaseCustomerTools databaseCustomerTools,
            CustomerRepository customerRepository
    ) {
        this.chatClient = chatClientBuilder.build();
        this.databaseCustomerTools = databaseCustomerTools;
        this.customerRepository = customerRepository;
    }

    /**
     * 1. Basic Gemini LLM call
     */
    public ChatResponse askGemini(String userPrompt, String systemPrompt) {
        var callSpec = chatClient.prompt().user(userPrompt);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            callSpec.system(systemPrompt);
        }

        String reply = callSpec.call().content();
        return new ChatResponse(reply, configuredModel);
    }

    /**
     * 2. Spring AI 2.0 Tool Calling with PostgreSQL.
     * Google Gemini dynamically calls DatabaseCustomerTools to fetch real data
     * from PostgreSQL and synthesizes an intelligent response.
     */
    public ChatResponse chatWithPostgresTools(String userPrompt) {
        String reply = chatClient.prompt()
                .system("""
                        You are an intelligent CRM database assistant.
                        You have access to PostgreSQL customer database tools.
                        Always use the tools to retrieve factual customer data before answering questions.
                        Be concise, professional, and accurate.
                        """)
                .user(userPrompt)
                .tools(databaseCustomerTools)
                .call()
                .content();

        return new ChatResponse(reply, configuredModel);
    }

    /**
     * 3. Spring AI 2.0 Structured Output:
     * Generates a strongly typed CustomerInsight DTO from PostgreSQL records.
     */
    public CustomerInsight generateCustomerInsight(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " not found.");
        }

        Customer customer = optionalCustomer.get();

        return chatClient.prompt()
                .system("You are an expert customer success analyst. Analyze the customer details and return a structured assessment.")
                .user("""
                      Customer Details:
                      Name: %s
                      Email: %s
                      Plan: %s
                      Status: %s
                      Notes: %s
                      """.formatted(
                              customer.getName(),
                              customer.getEmail(),
                              customer.getPlan(),
                              customer.getStatus(),
                              customer.getNotes()))
                .call()
                .entity(CustomerInsight.class);
    }
}
