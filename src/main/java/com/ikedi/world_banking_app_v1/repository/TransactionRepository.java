package com.ikedi.world_banking_app_v1.repository;

import com.ikedi.world_banking_app_v1.domain.entity.Transaction;
import com.ikedi.world_banking_app_v1.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(UserEntity user);
}
