package com.example.springai.tools;

import com.example.springai.model.Customer;
import com.example.springai.repository.CustomerRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring AI 2.0 Tool Calling Component.
 * Enables Google Gemini LLM to autonomously invoke PostgreSQL queries
 * to fetch real customer data when answering queries.
 */
@Component
public class DatabaseCustomerTools {

    private final CustomerRepository customerRepository;

    public DatabaseCustomerTools(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Tool(description = "List all registered customers from the PostgreSQL database")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Tool(description = "Search customers by their subscription plan such as Free, Pro, or Enterprise")
    public List<Customer> getCustomersByPlan(
            @ToolParam(description = "The subscription plan name, e.g. Free, Pro, Enterprise") String plan
    ) {
        return customerRepository.findByPlanIgnoreCase(plan);
    }

    @Tool(description = "Find a customer by their exact email address")
    public Customer getCustomerByEmail(
            @ToolParam(description = "The email address of the customer") String email
    ) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    @Tool(description = "Find customers matching a name or keyword in PostgreSQL database")
    public List<Customer> searchCustomersByName(
            @ToolParam(description = "Partial or full customer name") String name
    ) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }
}
