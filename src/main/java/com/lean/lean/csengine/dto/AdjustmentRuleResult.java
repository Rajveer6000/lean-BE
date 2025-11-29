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
public class AdjustmentRuleResult {
    private Long adjustmentRuleId;
    private String ruleKey;
    private String displayName;
    private BigDecimal points;
    private Boolean applied;
    private String reason;
}
