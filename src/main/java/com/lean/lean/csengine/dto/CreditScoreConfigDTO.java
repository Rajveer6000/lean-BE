package com.lean.lean.csengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Credit Score Engine Configuration
 * Contains all the scoring rules and thresholds
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditScoreConfigDTO {

    private EngineConfigInfo engineConfig;
    private List<MainCategoryInfo> mainCategories;
    private List<CategoryTypeInfo> categoryTypes;
    private List<CategoryConfigInfo> categoryConfigs;
    private List<AdjustmentRuleInfo> adjustmentRules;
    private List<RiskTierInfo> riskTiers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngineConfigInfo {
        private Long id;
        private String name;
        private String description;
        private BigDecimal minScore;
        private BigDecimal maxScore;
        private BigDecimal scoreCap;
        private BigDecimal totalWeightage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MainCategoryInfo {
        private Long id;
        private String name;
        private String displayName;
        private BigDecimal weightage;
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTypeInfo {
        private Long id;
        private String name;
        private String displayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryConfigInfo {
        private Long id;
        private Long mainCategoryId;
        private String mainCategoryName;
        private Long categoryTypeId;
        private String categoryTypeName;
        private BigDecimal weightage;
        private BigDecimal minScore;
        private BigDecimal maxScore;
        private Object calculationFormula; // JSONB
        private List<ThresholdInfo> thresholds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThresholdInfo {
        private Long id;
        private BigDecimal minScore;
        private BigDecimal maxScore;
        private BigDecimal scoreValue;
        private String scoreValueType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdjustmentRuleInfo {
        private Long id;
        private String key;
        private String vendorKey;
        private String displayName;
        private BigDecimal points;
        private Integer displayOrder;
        private RuleConfigInfo config;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleConfigInfo {
        private BigDecimal min;
        private BigDecimal max;
        private BigDecimal value;
        private String valueType;
        private String mode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskTierInfo {
        private Long id;
        private String riskTier;
        private BigDecimal minScore;
        private BigDecimal maxScore;
        private String riskLevel;
        private BigDecimal rentLimitPercentage;
    }
}
