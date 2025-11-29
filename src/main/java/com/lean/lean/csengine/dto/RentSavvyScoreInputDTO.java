package com.lean.lean.csengine.dto;

import com.lean.lean.csengine.enums.DataSource;
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
    private DataSource salariedDataSource;

    private IncomeStreamDTO nonSalaried;
    private DataSource nonSalariedDataSource;

    private Integer dataMonthCount;
    private DataSource dataMonthCountSource;

    private Integer employmentTenureInMonths;
    private DataSource employmentTenureSource;

    private BonusIncomeDTO bonusIncome;
    private DataSource bonusIncomeSource;

    private BigDecimal declaredMonthlyExpense;
    private DataSource declaredMonthlyExpenseSource;

    private BigDecimal declaredMonthlyIncome;
    private DataSource declaredMonthlyIncomeSource;

    private BigDecimal averageMonthlyIncome;
    private DataSource averageMonthlyIncomeSource;

    private BigDecimal averageMonthlyExpense;
    private DataSource averageMonthlyExpenseSource;

    private Integer numberOfDependents;
    private DataSource numberOfDependentsSource;

    private Integer aecbScore;
    private DataSource aecbScoreSource;
}