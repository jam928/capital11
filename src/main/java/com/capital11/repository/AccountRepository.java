package com.capital11.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.capital11.domain.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByCustomerCid(Integer cid);

    /** Row-locks the account so concurrent deposits/withdrawals can't overwrite each other's balance. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.customer.cid = :cid")
    Optional<Account> findByCustomerCidForUpdate(Integer cid);
}
