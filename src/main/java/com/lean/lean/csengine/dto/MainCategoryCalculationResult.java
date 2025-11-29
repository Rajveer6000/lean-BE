package com.lean.lean.csengine.dto;

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
public class MainCategoryCalculationResult {
    private Long mainCategoryId;
    private String mainCategoryName;
    private BigDecimal mainCategoryWeightage;
    private BigDecimal sumOfCategoryTypeWeightedScores;
    private BigDecimal finalMainCategoryScore;
    private List<CategoryCalculationResult> categoryTypeResults;
    private String calculationDetails;
}
