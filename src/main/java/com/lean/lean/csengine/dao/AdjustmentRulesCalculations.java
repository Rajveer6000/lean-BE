package com.lean.lean.csengine.dao;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "AdjustmentRulesCalculations")
public class AdjustmentRulesCalculations {

    @Id
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "UserCalculationMasterId", referencedColumnName = "Id", nullable = false)
    private CreditScoreUserCalculationMaster userCalculationMaster;

    @ManyToOne
    @JoinColumn(name = "BonusPenaltyId", referencedColumnName = "Id", nullable = false)
    private CreditScoreAdjustmentRules bonusPenalty;

    @Column(name = "ConditionMet", nullable = false)
    private Boolean conditionMet;

    @Type(JsonBinaryType.class)
    @Column(name = "ConditionValues", columnDefinition = "jsonb")
    private Object conditionValues;

    @Column(name = "PointsAwarded", precision = 5, scale = 2)
    private BigDecimal pointsAwarded;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CreatedBy", length = 255)
    private String createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}
