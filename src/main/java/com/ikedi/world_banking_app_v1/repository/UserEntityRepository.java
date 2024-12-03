package com.ikedi.world_banking_app_v1.repository;

import com.ikedi.world_banking_app_v1.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

    boolean existsByAccountNumber(String accountNumber);
}
