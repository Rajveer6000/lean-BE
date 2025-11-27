package com.lean.lean.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentSavvyScoreDTO {
    private Long userId;
    private Double finalScore;
    private String riskTier;
    private Map<String, Double> breakdowns;
    private Map<String, Object> sourceData;
}
