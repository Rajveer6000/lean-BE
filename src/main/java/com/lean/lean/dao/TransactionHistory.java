package com.lean.lean.dao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lean_user_id", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;

    private String transactionId;
    private Double amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}