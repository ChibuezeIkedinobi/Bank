package com.ikedi.world_banking_app_v1.domain.entity;

import com.ikedi.world_banking_app_v1.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
@SQLDelete(sql = "UPDATE transactions SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Transaction extends BaseClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; //CREDIT, DEBIT, TRANSFER

    private BigDecimal amount;

    private BigDecimal postTransactionBalance;

    private String description;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

}