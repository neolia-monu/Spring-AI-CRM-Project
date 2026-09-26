package com.example.springai.repository;

import com.example.springai.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository component of the MCR (Model-Controller-Repository) pattern.
 * Manages database persistence with PostgreSQL through Spring Data JPA.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    List<Customer> findByPlanIgnoreCase(String plan);

    List<Customer> findByStatusIgnoreCase(String status);

    List<Customer> findByNameContainingIgnoreCase(String nameFragment);
}
