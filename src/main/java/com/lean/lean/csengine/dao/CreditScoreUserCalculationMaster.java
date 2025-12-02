package com.lean.lean.csengine.dao;

import com.lean.lean.csengine.enums.CalculationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "CreditScoreUserCalculationMaster", indexes = {
        @Index(name = "creditscoreusercalculationmaster_userid_index", columnList = "UserId"),
        @Index(name = "creditscoreusercalculationmaster_createdat_index", columnList = "CreatedAt")
})
public class CreditScoreUserCalculationMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "UserId", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "EngineConfigID", referencedColumnName = "Id", nullable = false)
    private EngineConfig engineConfig;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "CalculationStatus", nullable = false)
    private CalculationStatus calculationStatus = CalculationStatus.PENDING;

    @Column(name = "BaseScore", precision = 5, scale = 2)
    private BigDecimal baseScore;

    @Column(name = "AdditionalScore", precision = 5, scale = 2)
    private BigDecimal additionalScore;

    @Column(name = "FinalScore", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "FinalScoreCapped", precision = 5, scale = 2)
    private BigDecimal finalScoreCapped;

    @ManyToOne
    @JoinColumn(name = "TierId", referencedColumnName = "Id")
    private RiskTierConfig tier;

    @Column(name = "RiskTier")
    private String riskTier;

    @Column(name = "RiskLevel", length = 50)
    private String riskLevel;

    @Column(name = "MaxRentLimit", precision = 12, scale = 2)
    private BigDecimal maxRentLimit;

    @Column(name = "ApprovalDecision", length = 50)
    private String approvalDecision;

    @Column(name = "ErrorMessage", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy")
    private Long createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private Long updatedBy;
}
