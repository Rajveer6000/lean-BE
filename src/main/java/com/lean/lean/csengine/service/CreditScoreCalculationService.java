package com.lean.lean.csengine.service;

import com.lean.lean.csengine.dao.*;
import com.lean.lean.csengine.dto.*;
import com.lean.lean.csengine.enums.CalculationStatus;
import com.lean.lean.csengine.enums.DataSource;
import com.lean.lean.csengine.repository.*;
import com.lean.lean.csengine.util.CalculationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating credit scores based on configuration and input data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreCalculationService {

    private final CreditScoreUserCalculationMasterRepository masterRepository;
    private final CreditScoreUserCalculationMetaInformationRepository metaInfoRepository;
    private final CategoryCalculationsRepository categoryCalculationsRepository;
    private final AdjustmentRulesCalculationsRepository adjustmentRulesCalculationsRepository;
    private final CreditScoreUserCalculationLogsRepository logsRepository;
    private final CreditScoreConfigService configService;
    private final CreditScoreUserCalculationMasterRepository creditScoreUserCalculationMasterRepository;
    private final EngineConfigRepository engineConfigRepository;
    private final ScoringMainCategoryRepository scoringMainCategoryRepository;
    private final CreditScoreAdjustmentRulesRepository creditScoreAdjustmentRulesRepository;

    /**
     * Main entry point for credit score calculation
     */
    @Transactional
    public Map<String, Object> calculateCreditScore(Long userId, RentSavvyScoreInputDTO inputDTO, Long engineConfigId) {
        log.info("Starting credit score calculation for user: {}", userId);

        try {
            // 1. Create master calculation record with PENDING status
            CreditScoreUserCalculationMaster master = createMasterRecord(userId, engineConfigId);

            // 2. Fetch configuration
            CreditScoreConfigDTO config = configService.getEngineConfiguration(engineConfigId);

            // 3. Update status to IN_PROGRESS
            updateMasterStatus(master, CalculationStatus.IN_PROGRESS);

            // 4. Save all meta information
            saveMetaInformation(master, inputDTO);
            logCalculationStep(master.getId(), "Meta information saved", "All input data persisted");

            // 5. Calculate category type scores
            List<CategoryCalculationResult> categoryResults = calculateCategoryScores(master.getId(), inputDTO, config);
            logCalculationStep(master.getId(), "Category type scores calculated",
                    "Total category types: " + categoryResults.size());

            // 6. Calculate main category scores by aggregating category type scores
            List<MainCategoryCalculationResult> mainCategoryResults = calculateMainCategoryScores(categoryResults,
                    config);
            BigDecimal totalCategoryScore = saveMainCategoryCalculations(master.getId(), mainCategoryResults);
            logCalculationStep(master.getId(), "Main category scores calculated",
                    "Total category score: " + totalCategoryScore);

            // 7. Apply adjustment rules
            List<AdjustmentRuleResult> adjustmentResults = applyAdjustmentRules(master.getId(), inputDTO, config);
            BigDecimal totalAdjustments = saveAdjustmentRuleCalculations(master.getId(), adjustmentResults);
            logCalculationStep(master.getId(), "Adjustment rules applied",
                    "Total adjustments: " + totalAdjustments);

            // 8. Calculate final score
            BigDecimal finalScore = calculateFinalScore(totalCategoryScore, totalAdjustments, config);
            finalScore = CalculationUtils.capScore(finalScore, config.getEngineConfig().getScoreCap());

            // 9. Determine risk tier
            String riskTier = determineRiskTier(finalScore, config);

            // 10. Update master with final score and complete status
            completeMasterRecord(master, finalScore, riskTier);
            logCalculationStep(master.getId(), "Calculation completed",
                    "Final score: " + finalScore + ", Risk Tier: " + riskTier);

            // 11. Build response
            Map<String, Object> response = new HashMap<>();
            response.put("calculationId", master.getId());
            response.put("userId", userId);
            response.put("finalScore", finalScore);
            response.put("riskTier", riskTier);
            response.put("categoryScores", categoryResults);
            response.put("mainCategoryScores", mainCategoryResults);
            response.put("adjustments", adjustmentResults);

            log.info("Credit score calculation completed successfully for user: {}", userId);
            return response;

        } catch (Exception e) {
            log.error("Error calculating credit score for user: {}", userId, e);
            throw new RuntimeException("Credit score calculation failed: " + e.getMessage(), e);
        }
    }

    private CreditScoreUserCalculationMaster createMasterRecord(Long userId, Long engineConfigId) {
        CreditScoreUserCalculationMaster master = new CreditScoreUserCalculationMaster();
        master.setUserId(userId);
        master.setEngineConfig(engineConfigRepository.findById(engineConfigId)
                .orElseThrow(() -> new RuntimeException("EngineConfig not found with id: " + engineConfigId)));
        master.setCalculationStatus(CalculationStatus.PENDING);
        master.setCreatedAt(LocalDateTime.now());

        return masterRepository.save(master);
    }

    private void updateMasterStatus(CreditScoreUserCalculationMaster master, CalculationStatus status) {
        master.setCalculationStatus(status);
        master.setUpdatedAt(LocalDateTime.now());
        masterRepository.save(master);
    }

    private void saveMetaInformation(CreditScoreUserCalculationMaster master, RentSavvyScoreInputDTO inputDTO) {
        List<CreditScoreUserCalculationMetaInformation> metaList = new ArrayList<>();

        // Save history and data month count
        metaList.add(createMetaInfo(master, "historyMonths",
                String.valueOf(inputDTO.getHistoryMonths()), DataSource.CALCULATED));
        metaList.add(createMetaInfo(master, "dataMonthCount",
                String.valueOf(inputDTO.getDataMonthCount()), inputDTO.getDataMonthCountSource()));

        // Save salary income (monthly)
        if (inputDTO.getSalaried() != null && inputDTO.getSalaried().getMonthlyIncome() != null) {
            List<BigDecimal> salaries = inputDTO.getSalaried().getMonthlyIncome();
            for (int i = 0; i < salaries.size(); i++) {
                metaList.add(createMetaInfo(master, "salary_income_m" + (i + 1),
                        salaries.get(i).toString(), inputDTO.getSalariedDataSource()));
            }
        }

        // Save non-salary income (monthly)
        if (inputDTO.getNonSalaried() != null && inputDTO.getNonSalaried().getMonthlyIncome() != null) {
            List<BigDecimal> nonSalaries = inputDTO.getNonSalaried().getMonthlyIncome();
            for (int i = 0; i < nonSalaries.size(); i++) {
                metaList.add(createMetaInfo(master, "non_salary_income_m" + (i + 1),
                        nonSalaries.get(i).toString(), inputDTO.getNonSalariedDataSource()));
            }
        }

        // Save expenses (monthly)
        if (inputDTO.getSalaried() != null && inputDTO.getSalaried().getMonthlyExpense() != null) {
            List<BigDecimal> expenses = inputDTO.getSalaried().getMonthlyExpense();
            for (int i = 0; i < expenses.size(); i++) {
                metaList.add(createMetaInfo(master, "expense_m" + (i + 1),
                        expenses.get(i).toString(), inputDTO.getSalariedDataSource()));
            }
        }

        // Save bonus income
        if (inputDTO.getBonusIncome() != null) {
            BonusIncomeDTO bonus = inputDTO.getBonusIncome();
            metaList.add(createMetaInfo(master, "rentalIncome",
                    bonus.getRentalIncome().toString(), inputDTO.getBonusIncomeSource()));
            metaList.add(createMetaInfo(master, "investmentIncome",
                    bonus.getInvestmentIncome().toString(), inputDTO.getBonusIncomeSource()));
            metaList.add(createMetaInfo(master, "familyAllowance",
                    bonus.getFamilyAllowance().toString(), inputDTO.getBonusIncomeSource()));
            metaList.add(createMetaInfo(master, "otherBonusIncome",
                    bonus.getOtherBonusIncome().toString(), inputDTO.getBonusIncomeSource()));
        }

        // Save aggregated values
        metaList.add(createMetaInfo(master, "averageMonthlyIncome",
                inputDTO.getAverageMonthlyIncome().toString(), inputDTO.getAverageMonthlyIncomeSource()));
        metaList.add(createMetaInfo(master, "averageMonthlyExpense",
                inputDTO.getAverageMonthlyExpense().toString(), inputDTO.getAverageMonthlyExpenseSource()));
        metaList.add(createMetaInfo(master, "declaredMonthlyIncome",
                inputDTO.getDeclaredMonthlyIncome().toString(), inputDTO.getDeclaredMonthlyIncomeSource()));
        metaList.add(createMetaInfo(master, "declaredMonthlyExpense",
                inputDTO.getDeclaredMonthlyExpense().toString(), inputDTO.getDeclaredMonthlyExpenseSource()));
        metaList.add(createMetaInfo(master, "numberOfDependents",
                String.valueOf(inputDTO.getNumberOfDependents()), inputDTO.getNumberOfDependentsSource()));
        metaList.add(createMetaInfo(master, "employmentTenureInMonths",
                String.valueOf(inputDTO.getEmploymentTenureInMonths()), inputDTO.getEmploymentTenureSource()));

        if (inputDTO.getAecbScore() != null) {
            metaList.add(createMetaInfo(master, "aecbScore",
                    String.valueOf(inputDTO.getAecbScore()), inputDTO.getAecbScoreSource()));
        }

        metaInfoRepository.saveAll(metaList);
    }

    private CreditScoreUserCalculationMetaInformation createMetaInfo(CreditScoreUserCalculationMaster master,
            String key,
            String value, DataSource source) {
        CreditScoreUserCalculationMetaInformation meta = new CreditScoreUserCalculationMetaInformation();
        meta.setUserCalculationMaster(master);
        meta.setUserId(master.getUserId());
        meta.setConfig(master.getEngineConfig());
        meta.setKeyName(key);
        meta.setValue(value);
        meta.setSource(source != null ? source.name() : null);
        meta.setCreatedAt(LocalDateTime.now());
        return meta;
    }

    private List<CategoryCalculationResult> calculateCategoryScores(Long masterId,
            RentSavvyScoreInputDTO inputDTO,
            CreditScoreConfigDTO config) {
        List<CategoryCalculationResult> results = new ArrayList<>();

        for (CreditScoreConfigDTO.CategoryConfigInfo categoryConfig : config.getCategoryConfigs()) {
            CategoryCalculationResult result = calculateCategoryScore(categoryConfig, inputDTO, config);
            if (result != null) {
                results.add(result);
            }
        }

        return results;
    }

    private CategoryCalculationResult calculateCategoryScore(CreditScoreConfigDTO.CategoryConfigInfo categoryConfig,
            RentSavvyScoreInputDTO inputDTO,
            CreditScoreConfigDTO config) {
        String categoryTypeName = categoryConfig.getCategoryTypeName();
        BigDecimal rawValue = null;
        BigDecimal rawScore = null;
        String details = "";

        switch (categoryTypeName) {
            case "Salary":
                if (inputDTO.getSalaried() != null) {
                    rawValue = CalculationUtils.calculateVariance(inputDTO.getSalaried().getMonthlyIncome());
                    rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                    details = "Salary variance: " + rawValue + "%";
                }
                break;

            case "EmploymentTenure":
                rawValue = BigDecimal.valueOf(inputDTO.getEmploymentTenureInMonths());
                rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                details = "Tenure: " + rawValue + " months";
                break;

            case "AECBScore":
                if (inputDTO.getAecbScore() != null) {
                    rawValue = BigDecimal.valueOf(inputDTO.getAecbScore());
                    rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                    details = "AECB Score: " + rawValue;
                }
                break;

            case "DebtToIncome":
                if (inputDTO.getAverageMonthlyIncome() != null &&
                        inputDTO.getAverageMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
                    rawValue = inputDTO.getAverageMonthlyExpense()
                            .divide(inputDTO.getAverageMonthlyIncome(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                    details = "DTI: " + rawValue + "%";
                }
                break;

            case "ExpenseVariance":
                if (inputDTO.getSalaried() != null) {
                    rawValue = CalculationUtils.calculateVariance(inputDTO.getSalaried().getMonthlyExpense());
                    rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                    details = "Expense variance: " + rawValue + "%";
                }
                break;

            case "SalaryMismatch":
                rawValue = CalculationUtils.calculatePercentageDifference(
                        inputDTO.getDeclaredMonthlyIncome(),
                        inputDTO.getAverageMonthlyIncome());
                rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                details = "Salary mismatch: " + rawValue + "%";
                break;

            case "ExpenseMismatch":
                rawValue = CalculationUtils.calculatePercentageDifference(
                        inputDTO.getDeclaredMonthlyExpense(),
                        inputDTO.getAverageMonthlyExpense());
                rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                details = "Expense mismatch: " + rawValue + "%";
                break;

            case "NumberOfDependents":
                rawValue = BigDecimal.valueOf(inputDTO.getNumberOfDependents());
                rawScore = CalculationUtils.findThresholdScore(rawValue, categoryConfig.getThresholds());
                details = "Dependents: " + rawValue;
                break;
        }

        if (rawScore != null) {
            BigDecimal weightedScore = CalculationUtils.applyWeightage(rawScore, categoryConfig.getWeightage());

            return CategoryCalculationResult.builder()
                    .categoryConfigId(categoryConfig.getId())
                    .mainCategoryId(categoryConfig.getMainCategoryId())
                    .mainCategoryName(categoryConfig.getMainCategoryName())
                    .categoryTypeName(categoryTypeName)
                    .rawValue(rawValue)
                    .rawScore(rawScore)
                    .weightage(categoryConfig.getWeightage())
                    .weightedScore(weightedScore)
                    .calculationDetails(details)
                    .build();
        }

        return null;
    }

    private BigDecimal saveCategoryCalculations(Long masterId, List<CategoryCalculationResult> results) {
        BigDecimal total = BigDecimal.ZERO;

        for (CategoryCalculationResult result : results) {
            CategoryCalculations calc = new CategoryCalculations();
            calc.setUserCalculationMaster(creditScoreUserCalculationMasterRepository.getById(masterId));

            // Set the main category using the mainCategoryId from result
            ScoringMainCategory mainCategory = scoringMainCategoryRepository.findById(result.getMainCategoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Main category not found with id: " + result.getMainCategoryId()));
            calc.setMainCategory(mainCategory);

            // Store calculation details as inputValues (JSON)
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("rawValue", result.getRawValue());
            inputData.put("rawScore", result.getRawScore());
            inputData.put("weightedScore", result.getWeightedScore());
            inputData.put("weightage", result.getWeightage());
            inputData.put("categoryTypeName", result.getCategoryTypeName());
            inputData.put("calculationDetails", result.getCalculationDetails());
            calc.setInputValues(inputData);

            // Set the weighted score as the final score
            calc.setScore(result.getWeightedScore());
            calc.setCreatedAt(LocalDateTime.now());

            categoryCalculationsRepository.save(calc);
            total = total.add(result.getWeightedScore());
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate main category scores by aggregating category type scores
     * Formula: Main Category Score = (Sum of Category Type Weighted Scores) * (Main
     * Category Weightage / 100)
     */
    private List<MainCategoryCalculationResult> calculateMainCategoryScores(
            List<CategoryCalculationResult> categoryResults,
            CreditScoreConfigDTO config) {

        List<MainCategoryCalculationResult> mainCategoryResults = new ArrayList<>();

        // Group category results by main category
        Map<Long, List<CategoryCalculationResult>> groupedByMainCategory = categoryResults.stream()
                .collect(Collectors.groupingBy(CategoryCalculationResult::getMainCategoryId));

        // Calculate score for each main category
        for (Map.Entry<Long, List<CategoryCalculationResult>> entry : groupedByMainCategory.entrySet()) {
            Long mainCategoryId = entry.getKey();
            List<CategoryCalculationResult> categoryTypesInMainCategory = entry.getValue();

            // Get main category info from config
            CreditScoreConfigDTO.MainCategoryInfo mainCategoryInfo = config.getMainCategories().stream()
                    .filter(mc -> mc.getId().equals(mainCategoryId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Main category not found with id: " + mainCategoryId));

            // Sum all category type weighted scores within this main category
            BigDecimal sumOfCategoryTypeWeightedScores = categoryTypesInMainCategory.stream()
                    .map(CategoryCalculationResult::getWeightedScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Apply main category weightage: (sum of category type weighted scores) * (main
            // category weightage / 100)
            BigDecimal finalMainCategoryScore = sumOfCategoryTypeWeightedScores
                    .multiply(mainCategoryInfo.getWeightage())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            // Build calculation details
            StringBuilder details = new StringBuilder();
            details.append("Category Types: ");
            for (CategoryCalculationResult cat : categoryTypesInMainCategory) {
                details.append(cat.getCategoryTypeName())
                        .append(" (weighted: ").append(cat.getWeightedScore()).append("), ");
            }
            details.append("Sum: ").append(sumOfCategoryTypeWeightedScores)
                    .append(", Main Weightage: ").append(mainCategoryInfo.getWeightage()).append("%")
                    .append(", Final: ").append(finalMainCategoryScore);

            MainCategoryCalculationResult mainCategoryResult = MainCategoryCalculationResult.builder()
                    .mainCategoryId(mainCategoryId)
                    .mainCategoryName(mainCategoryInfo.getName())
                    .mainCategoryWeightage(mainCategoryInfo.getWeightage())
                    .sumOfCategoryTypeWeightedScores(sumOfCategoryTypeWeightedScores)
                    .finalMainCategoryScore(finalMainCategoryScore.setScale(4, RoundingMode.HALF_UP))
                    .categoryTypeResults(categoryTypesInMainCategory)
                    .calculationDetails(details.toString())
                    .build();

            mainCategoryResults.add(mainCategoryResult);

            log.info("Main Category: {}, Sum of Category Types: {}, Weightage: {}%, Final Score: {}",
                    mainCategoryInfo.getName(), sumOfCategoryTypeWeightedScores,
                    mainCategoryInfo.getWeightage(), finalMainCategoryScore);
        }

        return mainCategoryResults;
    }

    /**
     * Save main category calculations and return total score
     */
    private BigDecimal saveMainCategoryCalculations(Long masterId,
            List<MainCategoryCalculationResult> mainCategoryResults) {
        BigDecimal totalScore = BigDecimal.ZERO;

        for (MainCategoryCalculationResult mainCatResult : mainCategoryResults) {
            // Save each category type calculation within the main category
            for (CategoryCalculationResult categoryTypeResult : mainCatResult.getCategoryTypeResults()) {
                CategoryCalculations calc = new CategoryCalculations();
                calc.setUserCalculationMaster(creditScoreUserCalculationMasterRepository.getById(masterId));

                // Set the main category using the mainCategoryId from result
                ScoringMainCategory mainCategory = scoringMainCategoryRepository
                        .findById(mainCatResult.getMainCategoryId())
                        .orElseThrow(() -> new RuntimeException(
                                "Main category not found with id: " + mainCatResult.getMainCategoryId()));
                calc.setMainCategory(mainCategory);

                // Store calculation details as inputValues (JSON)
                Map<String, Object> inputData = new HashMap<>();
                inputData.put("rawValue", categoryTypeResult.getRawValue());
                inputData.put("rawScore", categoryTypeResult.getRawScore());
                inputData.put("categoryTypeWeightedScore", categoryTypeResult.getWeightedScore());
                inputData.put("categoryTypeWeightage", categoryTypeResult.getWeightage());
                inputData.put("categoryTypeName", categoryTypeResult.getCategoryTypeName());
                inputData.put("calculationDetails", categoryTypeResult.getCalculationDetails());
                inputData.put("mainCategoryWeightage", mainCatResult.getMainCategoryWeightage());
                inputData.put("sumOfCategoryTypeWeightedScores", mainCatResult.getSumOfCategoryTypeWeightedScores());
                inputData.put("finalMainCategoryScore", mainCatResult.getFinalMainCategoryScore());
                calc.setInputValues(inputData);

                // Set the final main category score (all records in the same main category will
                // have the same score)
                calc.setScore(mainCatResult.getFinalMainCategoryScore());
                calc.setCreatedAt(LocalDateTime.now());

                categoryCalculationsRepository.save(calc);
            }

            // Add this main category's final score to the total
            totalScore = totalScore.add(mainCatResult.getFinalMainCategoryScore());
        }

        return totalScore.setScale(2, RoundingMode.HALF_UP);
    }

    private List<AdjustmentRuleResult> applyAdjustmentRules(Long masterId,
            RentSavvyScoreInputDTO inputDTO,
            CreditScoreConfigDTO config) {
        List<AdjustmentRuleResult> results = new ArrayList<>();

        for (CreditScoreConfigDTO.AdjustmentRuleInfo rule : config.getAdjustmentRules()) {
            AdjustmentRuleResult result = evaluateAdjustmentRule(rule, inputDTO);
            results.add(result);
        }

        return results;
    }

    private AdjustmentRuleResult evaluateAdjustmentRule(CreditScoreConfigDTO.AdjustmentRuleInfo rule,
            RentSavvyScoreInputDTO inputDTO) {
        boolean applied = false;
        String reason = "Not applicable";

        switch (rule.getKey()) {
            case "SavingsRateBonus":
                // Calculate savings rate: (Income - Expense) / Income × 100
                if (inputDTO.getAverageMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal savings = inputDTO.getAverageMonthlyIncome()
                            .subtract(inputDTO.getAverageMonthlyExpense());
                    BigDecimal savingsRate = savings.divide(inputDTO.getAverageMonthlyIncome(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

                    if (savingsRate.compareTo(BigDecimal.valueOf(20)) >= 0) {
                        applied = true;
                        reason = "Savings rate: " + savingsRate + "%";
                    }
                }
                break;

            case "RentalIncomeBonus":
                if (inputDTO.getBonusIncome() != null &&
                        inputDTO.getBonusIncome().getRentalIncome().compareTo(BigDecimal.ZERO) > 0) {
                    applied = true;
                    reason = "Rental income: " + inputDTO.getBonusIncome().getRentalIncome();
                }
                break;

            case "InvestmentIncomeBonus":
                if (inputDTO.getBonusIncome() != null &&
                        inputDTO.getBonusIncome().getInvestmentIncome().compareTo(BigDecimal.ZERO) > 0) {
                    applied = true;
                    reason = "Investment income: " + inputDTO.getBonusIncome().getInvestmentIncome();
                }
                break;

            case "FamilyAllowanceBonus":
                if (inputDTO.getBonusIncome() != null &&
                        inputDTO.getBonusIncome().getFamilyAllowance().compareTo(BigDecimal.ZERO) > 0) {
                    applied = true;
                    reason = "Family allowance: " + inputDTO.getBonusIncome().getFamilyAllowance();
                }
                break;

            case "HighExpenseVariancePenalty":
                if (inputDTO.getSalaried() != null) {
                    BigDecimal variance = CalculationUtils
                            .calculateVariance(inputDTO.getSalaried().getMonthlyExpense());
                    if (variance.compareTo(BigDecimal.valueOf(40)) >= 0) {
                        applied = true;
                        reason = "High expense variance: " + variance + "%";
                    }
                }
                break;
        }

        return AdjustmentRuleResult.builder()
                .adjustmentRuleId(rule.getId())
                .ruleKey(rule.getKey())
                .displayName(rule.getDisplayName())
                .points(applied ? rule.getPoints() : BigDecimal.ZERO)
                .applied(applied)
                .reason(reason)
                .build();
    }

    private BigDecimal saveAdjustmentRuleCalculations(Long masterId, List<AdjustmentRuleResult> results) {
        BigDecimal total = BigDecimal.ZERO;

        for (AdjustmentRuleResult result : results) {
            AdjustmentRulesCalculations calc = new AdjustmentRulesCalculations();
            calc.setUserCalculationMaster(creditScoreUserCalculationMasterRepository.getById(masterId));

            // Set the bonusPenalty relationship using the adjustment rule ID from result
            CreditScoreAdjustmentRules adjustmentRule = creditScoreAdjustmentRulesRepository
                    .findById(result.getAdjustmentRuleId())
                    .orElseThrow(() -> new RuntimeException(
                            "Adjustment rule not found with id: " + result.getAdjustmentRuleId()));
            calc.setBonusPenalty(adjustmentRule);

            // Set whether the condition was met (applied)
            calc.setConditionMet(result.getApplied());

            // Store condition details as JSON
            Map<String, Object> conditionData = new HashMap<>();
            conditionData.put("ruleKey", result.getRuleKey());
            conditionData.put("displayName", result.getDisplayName());
            conditionData.put("reason", result.getReason());
            conditionData.put("applied", result.getApplied());
            calc.setConditionValues(conditionData);

            // Set points awarded (only if applied, otherwise zero)
            calc.setPointsAwarded(result.getApplied() ? result.getPoints() : BigDecimal.ZERO);
            calc.setCreatedAt(LocalDateTime.now());

            adjustmentRulesCalculationsRepository.save(calc);

            if (result.getApplied()) {
                total = total.add(result.getPoints());
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinalScore(BigDecimal categoryScore, BigDecimal adjustments,
            CreditScoreConfigDTO config) {
        return categoryScore.add(adjustments).setScale(2, RoundingMode.HALF_UP);
    }

    private String determineRiskTier(BigDecimal finalScore, CreditScoreConfigDTO config) {
        for (CreditScoreConfigDTO.RiskTierInfo tier : config.getRiskTiers()) {
            if (finalScore.compareTo(tier.getMinScore()) >= 0 &&
                    finalScore.compareTo(tier.getMaxScore()) <= 0) {
                return tier.getRiskTier();
            }
        }
        return "Unknown";
    }

    private void completeMasterRecord(CreditScoreUserCalculationMaster master,
            BigDecimal finalScore, String riskTier) {
        master.setFinalScore(finalScore);
        master.setRiskTier(riskTier);
        master.setCalculationStatus(CalculationStatus.COMPLETED);
        master.setUpdatedAt(LocalDateTime.now());
        masterRepository.save(master);
    }

    private void logCalculationStep(Long masterId, String step, String details) {
        CreditScoreUserCalculationLogs log = new CreditScoreUserCalculationLogs();
        log.setUserCalculationMaster(creditScoreUserCalculationMasterRepository.getById(masterId));
        log.setLogMessage(step);
        log.setLogMessage(details);
        log.setCreatedAt(LocalDateTime.now());
        logsRepository.save(log);
    }
}
