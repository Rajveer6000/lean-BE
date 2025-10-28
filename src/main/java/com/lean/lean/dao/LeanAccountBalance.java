package com.lean.lean.dao;
import com.lean.lean.enums.AccountType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "lean_account_balance")
public class LeanAccountBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "AccountId", referencedColumnName = "id", nullable = false)
    private LeanAccount leanAccount;

    @Column(name = "balance")
    private Double balance;
    @Column(name = "CurrencyCode")
    private String currencyCode;
    @Column(name = "AccountType")
    private AccountType accountType;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}