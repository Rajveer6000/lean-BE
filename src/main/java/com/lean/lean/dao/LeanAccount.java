package com.lean.lean.dao;
import com.lean.lean.enums.AccountType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class LeanAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lean_user_id", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;

    private String accountName;
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private String iban;
    private String accountNumber;
    private Double creditLimit;
    private Date nextPaymentDueDate;
    private Double nextPaymentDueAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}