package com.lean.lean.csengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCalculationRequestDTO {
    private Long userId;
    private Integer historyMonths;
    private BigDecimal declaredMonthlyIncome;
    private BigDecimal declaredMonthlyExpense;
    private Integer employmentTenureInMonths;
    private Integer numberOfDependents;
    private Integer aecbScore;
}
