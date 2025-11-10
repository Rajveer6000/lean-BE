package com.lean.lean.dao;
import com.lean.lean.enums.AccountType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "lean_account_balance")
public class LeanAccountBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "accountid", referencedColumnName = "id", nullable = false)
    private LeanAccount leanAccount;

    @Column(name = "balance")
    private Double balance;
    @Column(name = "currencycode")
    private String currencyCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "accounttype")
    private AccountType accountType;

    @Column(name = "createdat")
    private LocalDateTime createdAt;
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}
