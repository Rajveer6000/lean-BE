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
public class MonthlyDataDTO {
    private BigDecimal amount;
    private Boolean isComplete;
    private Integer month; // Month number (1-12, as returned by Lean API)
    private Integer year; // Year (e.g., 2024)
}
