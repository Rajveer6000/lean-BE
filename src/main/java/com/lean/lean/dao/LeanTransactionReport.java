package com.lean.lean.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lean_transaction_reports")
public class LeanTransactionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;

    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "to_date")
    private LocalDateTime toDate;

    @Column(name = "total_transactions")
    private Integer totalTransactions;

    @Column(name = "total_income", precision = 18, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "total_expenses", precision = 18, scale = 2)
    private BigDecimal totalExpenses;

    @Column(name = "net_balance", precision = 18, scale = 2)
    private BigDecimal netBalance;

    @Column(name = "report_file_url", length = 500)
    private String reportFileUrl;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
