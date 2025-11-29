package com.lean.lean.csengine.service;

import com.lean.lean.csengine.dao.*;
import com.lean.lean.csengine.dto.CreditScoreConfigDTO;
import com.lean.lean.csengine.dto.CreditScoreConfigDTO.*;
import com.lean.lean.csengine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to fetch Credit Score Engine Configuration
 */
@Service
@RequiredArgsConstructor
public class CreditScoreConfigService {

    private final EngineConfigRepository engineConfigRepository;
    private final ScoringMainCategoryRepository scoringMainCategoryRepository;
    private final ScoringCategoryTypeRepository scoringCategoryTypeRepository;
    private final ScoringCategoryConfigMasterRepository scoringCategoryConfigMasterRepository;
    private final ScoringCategoryConfigThresholdRepository scoringCategoryConfigThresholdRepository;
    private final CreditScoreAdjustmentRulesRepository creditScoreAdjustmentRulesRepository;
    private final CreditScoreAdjustmentRulesConfigRepository creditScoreAdjustmentRulesConfigRepository;
    private final RiskTierConfigRepository riskTierConfigRepository;

    /**
     * Get complete engine configuration by ID
     * 
     * @param engineConfigId Engine Configuration ID
     * @return Complete configuration DTO
     */
    @Transactional(readOnly = true)
    public CreditScoreConfigDTO getEngineConfiguration(Long engineConfigId) {
        // Fetch engine config
        EngineConfig engineConfig = engineConfigRepository.findById(engineConfigId)
                .orElseThrow(() -> new RuntimeException("Engine config not found with ID: " + engineConfigId));

        // Fetch all related data
        List<ScoringMainCategory> mainCategories = scoringMainCategoryRepository
                .findByEngineConfigIdOrderByDisplayOrderAsc(engineConfigId);
        List<ScoringCategoryType> categoryTypes = scoringCategoryTypeRepository.findAll();
        List<ScoringCategoryConfigMaster> categoryConfigMasters = scoringCategoryConfigMasterRepository
                .findByEngineConfigId(engineConfigId);
        List<CreditScoreAdjustmentRules> adjustmentRules = creditScoreAdjustmentRulesRepository
                .findByEngineConfigIdOrderByDisplayOrderAsc(engineConfigId);
        List<RiskTierConfig> riskTiers = riskTierConfigRepository.findByEngineConfigId(engineConfigId);

        // Build DTO
        return CreditScoreConfigDTO.builder()
                .engineConfig(mapEngineConfig(engineConfig))
                .mainCategories(mapMainCategories(mainCategories))
                .categoryTypes(mapCategoryTypes(categoryTypes))
                .categoryConfigs(mapCategoryConfigs(categoryConfigMasters))
                .adjustmentRules(mapAdjustmentRules(adjustmentRules))
                .riskTiers(mapRiskTiers(riskTiers))
                .build();
    }

    private EngineConfigInfo mapEngineConfig(EngineConfig entity) {
        return EngineConfigInfo.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .minScore(entity.getMinScore())
                .maxScore(entity.getMaxScore())
                .scoreCap(entity.getScoreCap())
                .totalWeightage(entity.getTotalWeightage())
                .build();
    }

    private List<MainCategoryInfo> mapMainCategories(List<ScoringMainCategory> entities) {
        return entities.stream()
                .map(entity -> MainCategoryInfo.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .displayName(entity.getDisplayName())
                        .weightage(entity.getWeightage())
                        .displayOrder(entity.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CategoryTypeInfo> mapCategoryTypes(List<ScoringCategoryType> entities) {
        return entities.stream()
                .map(entity -> CategoryTypeInfo.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .displayName(entity.getDisplayName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CategoryConfigInfo> mapCategoryConfigs(List<ScoringCategoryConfigMaster> entities) {
        return entities.stream()
                .map(entity -> {
                    // Fetch thresholds for this config
                    List<ScoringCategoryConfigThreshold> thresholds = scoringCategoryConfigThresholdRepository
                            .findByCategoryConfigMasterId(entity.getId());

                    return CategoryConfigInfo.builder()
                            .id(entity.getId())
                            .mainCategoryId(entity.getMainCategory().getId())
                            .mainCategoryName(entity.getMainCategory().getName())
                            .categoryTypeId(entity.getCategoryType().getId())
                            .categoryTypeName(entity.getCategoryType().getName())
                            .weightage(entity.getWeightage())
                            .minScore(entity.getMinScore())
                            .maxScore(entity.getMaxScore())
                            .calculationFormula(entity.getCalculationFormula())
                            .thresholds(mapThresholds(thresholds))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ThresholdInfo> mapThresholds(List<ScoringCategoryConfigThreshold> entities) {
        return entities.stream()
                .map(entity -> ThresholdInfo.builder()
                        .id(entity.getId())
                        .minScore(entity.getMinScore())
                        .maxScore(entity.getMaxScore())
                        .scoreValue(entity.getScoreValue())
                        .scoreValueType(entity.getScoreValueType())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AdjustmentRuleInfo> mapAdjustmentRules(List<CreditScoreAdjustmentRules> entities) {
        return entities.stream()
                .map(entity -> {
                    // Fetch rule config (usually 1 per rule)
                    List<CreditScoreAdjustmentRulesConfig> configs = creditScoreAdjustmentRulesConfigRepository
                            .findByAdjustmentRulesId(entity.getId());

                    RuleConfigInfo configInfo = null;
                    if (!configs.isEmpty()) {
                        CreditScoreAdjustmentRulesConfig config = configs.get(0);
                        configInfo = RuleConfigInfo.builder()
                                .min(config.getMin())
                                .max(config.getMax())
                                .value(config.getValue())
                                .valueType(config.getValueType())
                                .mode(config.getMode())
                                .build();
                    }

                    return AdjustmentRuleInfo.builder()
                            .id(entity.getId())
                            .key(entity.getKey())
                            .vendorKey(entity.getVendorKey())
                            .displayName(entity.getDisplayName())
                            .points(entity.getPoints())
                            .displayOrder(entity.getDisplayOrder())
                            .config(configInfo)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RiskTierInfo> mapRiskTiers(List<RiskTierConfig> entities) {
        return entities.stream()
                .map(entity -> RiskTierInfo.builder()
                        .id(entity.getId())
                        .riskTier(entity.getRiskTier())
                        .minScore(entity.getMinScore())
                        .maxScore(entity.getMaxScore())
                        .riskLevel(entity.getRiskLevel())
                        .rentLimitPercentage(entity.getRentLimitPercentage())
                        .build())
                .collect(Collectors.toList());
    }
}
