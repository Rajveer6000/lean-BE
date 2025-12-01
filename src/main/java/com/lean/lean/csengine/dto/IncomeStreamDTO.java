package com.lean.lean.csengine.dto;

import com.lean.lean.csengine.enums.IncomeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeStreamDTO {
    private IncomeType type;
    private List<MonthlyDataDTO> monthlyIncome;
    private List<MonthlyDataDTO> monthlyExpense;
}