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
@Table(name = "income_insight_reports")
public class IncomeInsightReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "income_type", length = 100)
    private String incomeType;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "average_monthly_amount", precision = 18, scale = 2)
    private BigDecimal averageMonthlyAmount;

    @Column(name = "txn_count")
    private Integer txnCount;

    @Column(name = "first_date")
    private LocalDateTime firstDate;

    @Column(name = "last_date")
    private LocalDateTime lastDate;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "report_file_url", length = 500)
    private String reportFileUrl;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
