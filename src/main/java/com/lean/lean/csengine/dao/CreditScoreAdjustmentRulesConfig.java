package com.lean.lean.csengine.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@Table(name = "CreditScoreAdjustmentRulesConfig")
public class CreditScoreAdjustmentRulesConfig {

    @Id
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "AdjustmentRulesID", referencedColumnName = "Id", nullable = false)
    private CreditScoreAdjustmentRules adjustmentRules;

    @Column(name = "Min", nullable = false, precision = 8, scale = 2)
    private BigDecimal min;

    @Column(name = "Max", nullable = false, precision = 8, scale = 2)
    private BigDecimal max;

    @Column(name = "Value", precision = 8, scale = 2)
    private BigDecimal value;

    @Column(name = "ValueType", nullable = false, length = 255)
    private String valueType;

    @Column(name = "Mode", nullable = false, length = 255)
    private String mode;
}
