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
public class RentSavvyScoreInputDTO {
    private Long userId;
    private Integer historyMonths;
    private IncomeStreamDTO salaried;
    private IncomeStreamDTO nonSalaried;
    private Integer dataMonthCount;
    private Integer employmentTenureInMonths;
    private BonusIncomeDTO bonusIncome;
    private BigDecimal declaredMonthlyExpense;
    private BigDecimal declaredMonthlyIncome;
    private BigDecimal averageMonthlyIncome;
    private BigDecimal averageMonthlyExpense;
    private Integer numberOfDependents;
    private Integer aecbScore;
}