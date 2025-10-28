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
@Table(name = "lean_accounts")
public class LeanAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "LeanUserId", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;

    @Column(name = "AccountName")
    private String accountName;
    @Column(name = "CurrencyCode")
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "AccountType")
    private AccountType accountType;

    @Column(name = "Iban")
    private String iban;
    @Column(name = "AccountNumber")
    private String accountNumber;
    @Column(name = "CreditLimit")
    private Double creditLimit;
    @Column(name = "NextPaymentDueDate")
    private Date nextPaymentDueDate;
    @Column(name = "NextPaymentDueAmount")
    private Double nextPaymentDueAmount;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;


}