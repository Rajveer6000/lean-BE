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
public class CategoryCalculationResult {
    private Long categoryConfigId;
    private Long mainCategoryId;
    private String mainCategoryName;
    private String categoryTypeName;
    private BigDecimal rawValue;
    private BigDecimal rawScore;
    private BigDecimal weightage;
    private BigDecimal weightedScore;
    private String calculationDetails;
}
