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
public class BonusIncomeDTO {
    private BigDecimal rentalIncome;
    private BigDecimal investmentIncome;
    private BigDecimal familyAllowance;
    private BigDecimal otherBonusIncome;
}