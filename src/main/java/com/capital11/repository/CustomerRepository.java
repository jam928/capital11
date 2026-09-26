package com.capital11.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capital11.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);
}
