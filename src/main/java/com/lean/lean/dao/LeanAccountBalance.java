package com.lean.lean.dao;
import com.lean.lean.enums.AccountType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Entity
@Data
@NoArgsConstructor
public class LeanAccountBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private LeanAccount leanAccount;

    private Double balance;
    private String currencyCode;
    private AccountType accountType;

    private Timestamp createdAt;
    private Timestamp updatedAt;
}