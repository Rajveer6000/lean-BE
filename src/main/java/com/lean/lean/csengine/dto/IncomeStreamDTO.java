package com.lean.lean.csengine.dto;

import com.lean.lean.csengine.enums.IncomeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeStreamDTO {
    private IncomeType type;
    private List<BigDecimal> monthlyIncome;
    private List<BigDecimal> monthlyExpense;
}