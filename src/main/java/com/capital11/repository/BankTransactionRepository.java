package com.capital11.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capital11.domain.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Integer> {

    List<BankTransaction> findByCustomerCidOrderByDateDesc(Integer cid);
}
