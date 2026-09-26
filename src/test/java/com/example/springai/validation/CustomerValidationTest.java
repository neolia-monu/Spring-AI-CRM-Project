package com.example.springai.validation;

import com.example.springai.dto.ChatRequest;
import com.example.springai.model.Customer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Production Security: Jakarta Bean Validation Tests")
public class CustomerValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation for a valid customer")
    public void testValidCustomer() {
        Customer customer = new Customer("Alice Smith", "alice@example.com", "Enterprise", "ACTIVE", "Key account");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertTrue(violations.isEmpty(), "Valid customer should produce 0 violations");
    }

    @Test
    @DisplayName("Should fail validation on invalid email and illegal plan")
    public void testInvalidCustomerAttributes() {
        Customer customer = new Customer("", "not-an-email", "SuperPlan", "UNKNOWN_STATUS", "Notes");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);

        assertFalse(violations.isEmpty(), "Invalid customer should produce violations");
        assertTrue(violations.size() >= 4, "Should have violations for name, email, plan, and status");

        Set<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(violatedProperties.contains("name"), "Should flag name violation");
        assertTrue(violatedProperties.contains("email"), "Should flag email violation");
        assertTrue(violatedProperties.contains("plan"), "Should flag plan violation");
        assertTrue(violatedProperties.contains("status"), "Should flag status violation");
    }

    @Test
    @DisplayName("Should validate ChatRequest message boundary constraints")
    public void testChatRequestValidation() {
        ChatRequest emptyRequest = new ChatRequest("");
        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(emptyRequest);
        assertFalse(violations.isEmpty(), "Blank chat message should fail validation");

        ChatRequest oversizedRequest = new ChatRequest("a".repeat(4001));
        violations = validator.validate(oversizedRequest);
        assertFalse(violations.isEmpty(), "Oversized prompt (>4000 chars) should fail validation");
    }
}
