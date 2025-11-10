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
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "leanuserid", referencedColumnName = "id", nullable = false)
    private LeanUser leanUser;

    @Column(name = "accountname")
    private String accountName;
    @Column(name = "currencycode")
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounttype")
    private AccountType accountType;

    @Column(name = "iban")
    private String iban;
    @Column(name = "accountnumber")
    private String accountNumber;
    @Column(name = "creditlimit")
    private Double creditLimit;
    @Column(name = "nextpaymentduedate")
    private Date nextPaymentDueDate;
    @Column(name = "nextpaymentdueamount")
    private Double nextPaymentDueAmount;

    @Column(name = "createdat")
    private LocalDateTime createdAt;
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;


}
