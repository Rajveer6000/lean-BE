package com.lean.lean.csengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.csengine.dto.BonusIncomeDTO;
import com.lean.lean.csengine.enums.DataSource;
import com.lean.lean.csengine.dto.CreditScoreConfigDTO;
import com.lean.lean.csengine.dto.IncomeStreamDTO;
import com.lean.lean.csengine.dto.MonthlyDataDTO;
import com.lean.lean.csengine.dto.RentSavvyScoreInputDTO;
import com.lean.lean.csengine.dto.ScoreCalculationRequestDTO;
import com.lean.lean.dao.LeanEntity;
import com.lean.lean.dao.User;
import com.lean.lean.repository.LeanEntityRepository;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.service.LeanReportService;
import com.lean.lean.util.LeanApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreImpl implements CreditScoreService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;

    @Autowired
    private LeanApiUtil leanApiUtil;

    @Autowired
    private LeanEntityRepository leanEntityRepository;

    @Autowired
    private LeanReportService leanReportService;

    @Autowired
    private CreditScoreConfigService creditScoreConfigService;

    @Autowired
    private CreditScoreCalculationService creditScoreCalculationService;

    private final ObjectMapper objectMapper;

    private final ExecutorService apiExecutor = Executors.newFixedThreadPool(10);

    @Override
    public Map<String, Object> calculateScore(ScoreCalculationRequestDTO request) {
        Long userId = request.getUserId();
        Integer historyMonths = request.getHistoryMonths();
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                    .orElseThrow(() -> new RuntimeException("LeanEntity not found for user"));

            LocalDate startLocalDate = LocalDate.now().minusMonths(historyMonths);
            String accessToken = leanApiUtil.getAccessToken();

            // Fetch only Income and Expense insights
            log.info("Fetching Income Insights for User {}", userId);
            Object incomeResponse = leanApiUtil.getIncomeInsights(
                    leanEntity.getEntityId(), startLocalDate, "ALL", accessToken);
            leanReportService.captureIncomeReport(userId, startLocalDate, "ALL", incomeResponse);

            log.info("Fetching Expense Insights for User {}", userId);
            Object expenseResponse = leanApiUtil.getExpensesInsights(
                    leanEntity.getEntityId(), startLocalDate, accessToken);
            leanReportService.captureExpenseReport(userId, startLocalDate, expenseResponse);

            // Parse JSON responses
            JsonNode incomeNode = parseJson(incomeResponse);
            JsonNode expenseNode = parseJson(expenseResponse);

            // Extract data using helper methods
            Integer dataMonthCount = calculateDataMonthCount(incomeNode, expenseNode);
            List<MonthlyDataDTO> monthlySalaryData = extractMonthlySalaryData(incomeNode, historyMonths);
            List<MonthlyDataDTO> monthlyNonSalaryData = extractMonthlyNonSalaryData(incomeNode, historyMonths);
            List<MonthlyDataDTO> monthlyExpenseData = extractMonthlyExpenseData(expenseNode, historyMonths);
            BonusIncomeDTO bonusIncome = extractBonusIncome(incomeNode);

            // Calculate average monthly income from complete months
            BigDecimal averageMonthlySalary = calculateAverageSalary(incomeNode);
            BigDecimal averageMonthlyExpense = extractAverageMonthlyExpense(expenseNode);
            log.info("Average Monthly Salary: {}", averageMonthlySalary);
            log.info("Average Monthly Expense: {}", averageMonthlyExpense);
            // Build salaried income DTO
            IncomeStreamDTO salariedIncome = IncomeStreamDTO.builder()
                    .type(com.lean.lean.csengine.enums.IncomeType.SALARIED)
                    .monthlyIncome(monthlySalaryData)
                    .monthlyExpense(monthlyExpenseData)
                    .build();

            // Build non-salaried income DTO (if non-salary income exists)
            IncomeStreamDTO nonSalariedIncome = null;
            if (!monthlyNonSalaryData.isEmpty()) {
                nonSalariedIncome = IncomeStreamDTO.builder()
                        .type(com.lean.lean.csengine.enums.IncomeType.NON_SALARIED)
                        .monthlyIncome(monthlyNonSalaryData)
                        .monthlyExpense(new ArrayList<>()) // Expenses not separated by income type
                        .build();
            }

            // Build and return the complete DTO with data sources
            RentSavvyScoreInputDTO inputDTO = RentSavvyScoreInputDTO.builder()
                    .userId(userId)
                    .historyMonths(historyMonths)
                    .salaried(salariedIncome)
                    .salariedDataSource(DataSource.LEAN_INCOME_API)
                    .nonSalaried(nonSalariedIncome)
                    .nonSalariedDataSource(nonSalariedIncome != null ? DataSource.LEAN_INCOME_API : null)
                    .bonusIncome(bonusIncome)
                    .bonusIncomeSource(bonusIncome != null ? DataSource.LEAN_INCOME_API : null)
                    .averageMonthlyIncome(averageMonthlySalary)
                    .averageMonthlyIncomeSource(DataSource.CALCULATED)
                    .averageMonthlyExpense(averageMonthlyExpense)
                    .averageMonthlyExpenseSource(DataSource.CALCULATED)
                    .declaredMonthlyIncome(request.getDeclaredMonthlyIncome())
                    .declaredMonthlyIncomeSource(DataSource.DEFAULT)
                    .declaredMonthlyExpense(request.getDeclaredMonthlyExpense())
                    .declaredMonthlyExpenseSource(DataSource.DEFAULT)
                    .employmentTenureInMonths(request.getEmploymentTenureInMonths())
                    .employmentTenureSource(DataSource.DEFAULT)
                    .numberOfDependents(request.getNumberOfDependents())
                    .numberOfDependentsSource(DataSource.DEFAULT)
                    .aecbScore(request.getAecbScore())
                    .aecbScoreSource(DataSource.AECB)
                    .dataMonthCount(dataMonthCount)
                    .dataMonthCountSource(DataSource.CALCULATED)
                    .build();


            // Calculate credit score using the calculation engine
            Map<String, Object> calculationResult = creditScoreCalculationService.calculateCreditScore(
                    userId, inputDTO, 1L);

            Map<String, Object> response = new HashMap<>();
            response.put("calculation", calculationResult);

            return response;
        } catch (Exception e) {
            log.error("Error calculating RentSavvy Score", e);
            throw new RuntimeException("Error calculating score: " + e.getMessage());
        }
    }

    /**
     * Calculate the count of months for which we have data
     * Counts only months with non-zero transaction amounts
     */
    private Integer calculateDataMonthCount(JsonNode incomeNode, JsonNode expenseNode) {
        try {
            int incomeMonthCount = 0;
            int expenseMonthCount = 0;

            // Count months with non-zero salary income
            JsonNode salaryMonthlyTotals = incomeNode.path("insights").path("salary")
                    .path("monthly_totals");
            if (salaryMonthlyTotals.isArray()) {
                for (JsonNode month : salaryMonthlyTotals) {
                    if (month.path("is_month_complete").asBoolean(false)) {
                        double amount = month.path("amount").asDouble(0.0);
                        if (amount > 0.0) {
                            incomeMonthCount++;
                        }
                    }
                }
            }

            // Also consider non-salary income months
            JsonNode nonSalaryMonthlyTotals = incomeNode.path("insights").path("non_salary")
                    .path("monthly_totals");
            if (nonSalaryMonthlyTotals.isArray()) {
                int nonSalaryMonths = 0;
                for (JsonNode month : nonSalaryMonthlyTotals) {
                    if (month.path("is_month_complete").asBoolean(false)) {
                        double amount = month.path("amount").asDouble(0.0);
                        if (amount > 0.0) {
                            nonSalaryMonths++;
                        }
                    }
                }
                // Use the maximum between salary and non-salary months
                incomeMonthCount = Math.max(incomeMonthCount, nonSalaryMonths);
            }

            // Count months with non-zero expense
            JsonNode expenseMonthlyTotals = expenseNode.path("insights").path("monthly_totals");
            if (expenseMonthlyTotals.isArray()) {
                for (JsonNode month : expenseMonthlyTotals) {
                    if (month.path("is_month_complete").asBoolean(false)) {
                        double amount = month.path("amount").asDouble(0.0);
                        if (amount > 0.0) {
                            expenseMonthCount++;
                        }
                    }
                }
            }

            // Return the maximum between income and expense month counts
            int maxMonthCount = Math.max(incomeMonthCount, expenseMonthCount);

            return maxMonthCount > 0 ? maxMonthCount : null;
        } catch (Exception e) {
            log.error("Error calculating data month count", e);
            return null;
        }
    }

    /**
     * Extract monthly salary data from complete months only
     */
    private List<MonthlyDataDTO> extractMonthlySalaryData(JsonNode incomeNode, int maxMonths) {
        List<MonthlyDataDTO> monthlySalaryList = new ArrayList<>();
        try {
            JsonNode monthlyTotals = incomeNode.path("insights").path("salary")
                    .path("monthly_totals");

            if (!monthlyTotals.isArray()) {
                return monthlySalaryList;
            }

            // Extract only complete months
            for (JsonNode month : monthlyTotals) {
                if (month.path("is_month_complete").asBoolean(false)) {
                    double amount = month.path("amount").asDouble(0.0);
                    Integer year = month.path("year").asInt(0);
                    Integer monthNum = month.path("month").asInt(0);
                    boolean isComplete = month.path("is_month_complete").asBoolean(false);

                    MonthlyDataDTO monthlyData = MonthlyDataDTO.builder()
                            .amount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP))
                            .isComplete(isComplete)
                            .month(monthNum > 0 ? monthNum : null)
                            .year(year > 0 ? year : null)
                            .build();
                    monthlySalaryList.add(monthlyData);
                }

                // Limit to maxMonths
                if (monthlySalaryList.size() >= maxMonths) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting monthly salary data", e);
        }
        return monthlySalaryList;
    }

    /**
     * Extract monthly non-salary data from complete months only
     */
    private List<MonthlyDataDTO> extractMonthlyNonSalaryData(JsonNode incomeNode, int maxMonths) {
        List<MonthlyDataDTO> monthlyNonSalaryList = new ArrayList<>();
        try {
            JsonNode monthlyTotals = incomeNode.path("insights").path("non_salary")
                    .path("monthly_totals");

            if (!monthlyTotals.isArray()) {
                return monthlyNonSalaryList;
            }

            // Extract only complete months
            for (JsonNode month : monthlyTotals) {
                if (month.path("is_month_complete").asBoolean(false)) {
                    double amount = month.path("amount").asDouble(0.0);
                    Integer year = month.path("year").asInt(0);
                    Integer monthNum = month.path("month").asInt(0);
                    boolean isComplete = month.path("is_month_complete").asBoolean(false);

                    MonthlyDataDTO monthlyData = MonthlyDataDTO.builder()
                            .amount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP))
                            .isComplete(isComplete)
                            .month(monthNum > 0 ? monthNum : null)
                            .year(year > 0 ? year : null)
                            .build();
                    monthlyNonSalaryList.add(monthlyData);
                }

                // Limit to maxMonths
                if (monthlyNonSalaryList.size() >= maxMonths) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting monthly non-salary data", e);
        }
        return monthlyNonSalaryList;
    }

    /**
     * Extract monthly expense data from complete months only
     */
    private List<MonthlyDataDTO> extractMonthlyExpenseData(JsonNode expenseNode, int maxMonths) {
        List<MonthlyDataDTO> monthlyExpenseList = new ArrayList<>();
        try {
            JsonNode monthlyTotals = expenseNode.path("insights").path("monthly_totals");

            if (!monthlyTotals.isArray()) {
                return monthlyExpenseList;
            }

            // Extract only complete months
            for (JsonNode month : monthlyTotals) {
                if (month.path("is_month_complete").asBoolean(false)) {
                    double amount = month.path("amount").asDouble(0.0);
                    Integer year = month.path("year").asInt(0);
                    Integer monthNum = month.path("month").asInt(0);
                    boolean isComplete = month.path("is_month_complete").asBoolean(false);

                    MonthlyDataDTO monthlyData = MonthlyDataDTO.builder()
                            .amount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP))
                            .isComplete(isComplete)
                            .month(monthNum > 0 ? monthNum : null)
                            .year(year > 0 ? year : null)
                            .build();
                    monthlyExpenseList.add(monthlyData);
                }

                // Limit to maxMonths
                if (monthlyExpenseList.size() >= maxMonths) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting monthly expense data", e);
        }
        return monthlyExpenseList;
    }

    /**
     * Extract and categorize bonus income from non-salary transactions
     */
    private BonusIncomeDTO extractBonusIncome(JsonNode incomeNode) {
        try {
            JsonNode transactions = incomeNode.path("insights").path("non_salary")
                    .path("transactions");

            if (!transactions.isArray() || transactions.isEmpty()) {
                return null;
            }

            BigDecimal rentalIncome = BigDecimal.ZERO;
            BigDecimal investmentIncome = BigDecimal.ZERO;
            BigDecimal familyAllowance = BigDecimal.ZERO;
            BigDecimal otherBonusIncome = BigDecimal.ZERO;

            for (JsonNode transaction : transactions) {
                double amount = transaction.path("amount").asDouble(0.0);
                String incomeType = transaction.path("income_source").path("type").asText("");

                BigDecimal amountDecimal = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);

                switch (incomeType) {
                    case "HOUSING_BENEFIT":
                        rentalIncome = rentalIncome.add(amountDecimal);
                        break;
                    case "INVESTMENT":
                        investmentIncome = investmentIncome.add(amountDecimal);
                        break;
                    case "FAMILY_ALLOWANCE":
                        familyAllowance = familyAllowance.add(amountDecimal);
                        break;
                    case "BONUS":
                    case "GIG_ECONOMY":
                    case "TRANSFER_FROM_OWN_APPLICATION":
                    case "CASH_DEPOSIT":
                    default:
                        otherBonusIncome = otherBonusIncome.add(amountDecimal);
                        break;
                }
            }

            // Return null if no bonus income found
            if (rentalIncome.compareTo(BigDecimal.ZERO) == 0 &&
                    investmentIncome.compareTo(BigDecimal.ZERO) == 0 &&
                    familyAllowance.compareTo(BigDecimal.ZERO) == 0 &&
                    otherBonusIncome.compareTo(BigDecimal.ZERO) == 0) {
                return null;
            }

            return BonusIncomeDTO.builder()
                    .rentalIncome(rentalIncome)
                    .investmentIncome(investmentIncome)
                    .familyAllowance(familyAllowance)
                    .otherBonusIncome(otherBonusIncome)
                    .build();
        } catch (Exception e) {
            log.error("Error extracting bonus income", e);
            return null;
        }
    }

    /**
     * Calculate average monthly salary from salary insights
     */
    private BigDecimal calculateAverageSalary(JsonNode incomeNode) {
        try {
            double averageMonthlySalary = incomeNode.path("insights").path("salary")
                    .path("total").path("average_monthly_amount").asDouble(0.0);

            return BigDecimal.valueOf(averageMonthlySalary).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error calculating average salary", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Extract average monthly expense from expense insights
     */
    private BigDecimal extractAverageMonthlyExpense(JsonNode expenseNode) {
        try {
            double averageMonthlyExpense = expenseNode.path("insights").path("total")
                    .path("average_monthly_amount").asDouble(0.0);

            return BigDecimal.valueOf(averageMonthlyExpense).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error extracting average monthly expense", e);
            return BigDecimal.ZERO;
        }
    }

    private JsonNode parseJson(Object response) {
        try {
            if (response instanceof String) {
                return objectMapper.readTree((String) response);
            } else {
                return objectMapper.valueToTree(response);
            }
        } catch (Exception e) {
            log.error("JSON Parsing error", e);
            return objectMapper.createObjectNode();
        }
    }
}
