package com.lean.lean.service.user;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanEntity;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.AddUserDTO;
import com.lean.lean.dto.LeanCustomerRegResponse;
import com.lean.lean.dto.RentSavvyScoreDTO;
import com.lean.lean.dto.UserDTO;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;

    @Autowired
    private  LeanApiUtil leanApiUtil;

    @Autowired
    private LeanEntityRepository leanEntityRepository;

    @Autowired
    private LeanReportService leanReportService;

    private final ObjectMapper objectMapper;

    private final ExecutorService apiExecutor = Executors.newFixedThreadPool(10);

    @Override
    public UserDTO registerUser(AddUserDTO addUserDTO) {
        User user = new User();
        user.setEmail(addUserDTO.getEmail());
        user.setPassword(addUserDTO.getPassword());
        user.setFirstName(addUserDTO.getFirstName());
        user.setLastName(addUserDTO.getLastName());
        user.setDateOfBirth(addUserDTO.getDateOfBirth());
        user.setGender(addUserDTO.getGender());
        user.setPhone(addUserDTO.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String accessToken = leanApiUtil.getAccessToken();

        LeanCustomerRegResponse leanCustomerRegResponse = leanApiUtil.createCustomerOnLean(savedUser, accessToken);
        LeanUser leanUser = new LeanUser();
        leanUser.setUser(savedUser);
        leanUser.setLeanUserId(leanCustomerRegResponse.getCustomer_id());
        leanUser.setCreatedAt(LocalDateTime.now());
        leanUser.setUpdatedAt(LocalDateTime.now());
        leanUserRepository.save(leanUser);

        return new UserDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getDateOfBirth(),
                savedUser.getGender(),
                savedUser.getPhone(),
                leanUser.getLeanUserId()
        );
    }

    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        List<UserDTO> allUserDTOList = users.stream()
                .map(u -> {
                    // Fetch LeanUser for the current user
                    LeanUser leanUser = leanUserRepository.findByUser(u);

                    // Create the UserDTO with leanUserId if it exists
                    return new UserDTO(
                            u.getId(),
                            u.getEmail(),
                            u.getPassword(),
                            u.getFirstName(),
                            u.getLastName(),
                            u.getDateOfBirth(),
                            u.getGender(),
                            u.getPhone(),
                            leanUser != null ? leanUser.getLeanUserId() : null  // Add leanUserId if available
                    );
                })
                .toList();
        log.info("Mapped UserDTOs: {}", allUserDTOList);
        return allUserDTOList;
    }



    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    @Override
    public RentSavvyScoreDTO calculateScore(Long userId, Integer historyMonths) {
        try {
            // --- 1. Fetch User & Entity ---
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                    .orElseThrow(() -> new RuntimeException("LeanEntity not found for user"));

            // --- 2. Date Setup ---
            LocalDate endLocalDate = LocalDate.now();
            LocalDate startLocalDate = endLocalDate.minusMonths(historyMonths);
            String accessToken = leanApiUtil.getAccessToken();

            // Initialize Map to hold all API responses for the DTO
            Map<String, Object> allApiResponses = new HashMap<>();

            // --- 3. Fetch Income ---
            log.info("Fetching Income Insights for User {}", userId);
            Object incomeResponse = leanApiUtil.getIncomeInsights(
                    leanEntity.getEntityId(), startLocalDate, "ALL", accessToken);
            allApiResponses.put("income_api", incomeResponse); // Add to response list

            // Async Report Capture
            CompletableFuture.runAsync(() ->
                    leanReportService.captureIncomeReport(userId, startLocalDate, "ALL", incomeResponse), apiExecutor);

            // --- 4. Fetch Expenses ---
            log.info("Fetching Expense Insights for User {}", userId);
            Object expenseResponse = leanApiUtil.getExpensesInsights(
                    leanEntity.getEntityId(), startLocalDate, accessToken);
            allApiResponses.put("expenses_api", expenseResponse); // Add to response list

            CompletableFuture.runAsync(() ->
                    leanReportService.captureExpenseReport(userId, startLocalDate, expenseResponse), apiExecutor);

            // --- 5. Fetch Accounts & Transactions ---
            log.info("Fetching Accounts for User {}", userId);
            Object accountsResponse = leanApiUtil.getUserAccounts(leanEntity.getEntityId(), accessToken);
            allApiResponses.put("accounts_api", accountsResponse); // Add to response list

            JsonNode accountsNode = parseJson(accountsResponse);
            JsonNode accountsList = accountsNode.path("payload").path("accounts");

            // Thread-safe map to collect transaction responses per account
            Map<String, Object> transactionResponses = new ConcurrentHashMap<>();
            List<CompletableFuture<Void>> transactionTasks = new ArrayList<>();

            if (accountsList.isArray()) {
                for (JsonNode account : accountsList) {
                    String accountId = account.path("account_id").asText();

                    // Create parallel task
                    CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                        try {
                            log.info("Fetching transactions for Account: {}", accountId);
                            Object trxResponse = leanApiUtil.getUserTransactions(
                                    leanEntity.getEntityId(), accountId, startLocalDate, endLocalDate, accessToken);

                            // Store response in map (key: accountId)
                            transactionResponses.put(accountId, trxResponse);

                            // Capture Report
                            leanReportService.captureTransactionReport(
                                    userId, accountId, startLocalDate, endLocalDate, trxResponse);
                        } catch (Exception e) {
                            log.error("Error fetching transactions for account {}", accountId, e);
                            transactionResponses.put(accountId, "Error: " + e.getMessage());
                        }
                    }, apiExecutor);

                    transactionTasks.add(task);
                }
            }

            // --- 6. Process Score Logic (While threads run) ---
            JsonNode incomeNode = parseJson(incomeResponse);
            JsonNode expenseNode = parseJson(expenseResponse);

            // A. Income Analysis
            JsonNode salaryMonthly = incomeNode.path("payload").path("insights").path("salary").path("monthly_totals");
            double m1 = 0, m2 = 0, m3 = 0, leanSalary = 0;
            if (salaryMonthly.isArray() && salaryMonthly.size() >= 3) {
                m1 = salaryMonthly.get(0).path("amount").asDouble();
                m2 = salaryMonthly.get(1).path("amount").asDouble();
                m3 = salaryMonthly.get(2).path("amount").asDouble();
                leanSalary = (m1 + m2 + m3) / 3;
            }

            // B. Secondary Income
            double nonSalary = incomeNode.path("payload").path("insights").path("non_salary")
                    .path("total").path("amount").asDouble();
            boolean hasBonus = nonSalary > 0;

            // C. DTI (Loan Obligations)
            double monthlyLoan = 0;
            JsonNode breakdowns = expenseNode.path("payload").path("insights").path("breakdown");
            if (breakdowns.isArray() && !breakdowns.isEmpty()) {
                JsonNode categories = breakdowns.get(0).path("breakdowns");
                for (JsonNode cat : categories) {
                    if ("LOAN".equalsIgnoreCase(cat.path("category").asText())) {
                        monthlyLoan = cat.path("average_monthly_amount").asDouble();
                        break;
                    }
                }
            }

            // D. Total Expenses
            double leanExpenses = expenseNode.path("payload").path("insights").path("total")
                    .path("average_monthly_amount").asDouble();

            // --- 7. Calculate Components ---
            Map<String, Double> scoreBreakdown = new HashMap<>();

            double incomeScore = calculateIncomeStabilityScore(m1, m2, m3);
            scoreBreakdown.put("Income Stability", incomeScore);

            double dtiRatio = (leanSalary > 0) ? (monthlyLoan / leanSalary) * 100 : 0;
            double dtiScore = calculateDTIScore(dtiRatio);
            scoreBreakdown.put("DTI Score", dtiScore);
            scoreBreakdown.put("DTI Ratio", dtiRatio);

            double aecbScore = calculateAECBScore( 733.0);
            scoreBreakdown.put("AECB Score", aecbScore);

            double expensePenalty = calculateExpenseVariancePenalty(leanExpenses, leanExpenses);
            scoreBreakdown.put("Expense Penalty", expensePenalty);

            double dependentScore = calculateDependentScore( 1);
            scoreBreakdown.put("Dependent Score", dependentScore);

            double bonusPoints = hasBonus ? 1.0 : 0.0;
            scoreBreakdown.put("Bonus Points", bonusPoints);

            // 8. Final Formula
            double finalScore = (incomeScore * 0.30) + (aecbScore * 0.25) + (dtiScore * 0.20) + (dependentScore * 0.05)
                    + bonusPoints + expensePenalty;
            finalScore = Math.min(10.0, Math.max(0.0, finalScore));

            // --- 9. WAIT FOR TRANSACTIONS ---
            // We must wait here to ensure 'transactionResponses' is fully populated before returning
            CompletableFuture.allOf(transactionTasks.toArray(new CompletableFuture[0])).join();

            // Add collected transactions to the source data map
            allApiResponses.put("transactions_api_by_account", transactionResponses);

            // --- 10. Return Result ---
            return RentSavvyScoreDTO.builder()
                    .userId(userId)
                    .finalScore(round(finalScore, 2))
                    .riskTier(getRiskTier(finalScore))
                    .breakdowns(scoreBreakdown)
                    .sourceData(allApiResponses) // Populating the new list/map
                    .build();

        } catch (Exception e) {
            log.error("Error calculating RentSavvy Score", e);
            throw new RuntimeException("Error calculating score: " + e.getMessage());
        }
    }

    // --- Helper Methods ---
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

    private double calculateIncomeStabilityScore(double m1, double m2, double m3) {
        if (m3 > 0 && m2 > 0 && m1 > 0) {
            if (m2 == m3 && m3 > m1) return 9.5;
            if (m3 > m2 && m2 == m1) return 7.5;
            double avg = (m1 + m2 + m3) / 3;
            double variancePct = ((Math.abs(m1 - avg) + Math.abs(m2 - avg) + Math.abs(m3 - avg)) / 3 / avg) * 100;
            if (variancePct < 10) return 9.0;
            if (variancePct < 20) return 7.0;
            return 4.0;
        }
        return 2.0;
    }

    private double calculateDTIScore(double dti) {
        if (dti < 25) return 10;
        if (dti < 35) return 9;
        if (dti < 45) return 8;
        if (dti < 55) return 7;
        if (dti < 65) return 6;
        if (dti < 75) return 5;
        return 3;
    }

    private double calculateAECBScore(double score) {
        if (score >= 850) return 10;
        if (score >= 700) return 9;
        if (score >= 650) return 7;
        if (score >= 550) return 4;
        return 1;
    }

    private double calculateExpenseVariancePenalty(double declared, double actual) {
        if (actual == 0) return 0;
        double pct = (Math.abs(declared - actual) / actual) * 100;
        if (pct <= 20) return 0.0;
        if (pct <= 30) return -1.0;
        if (pct <= 40) return -2.0;
        return -4.0;
    }

    private double calculateDependentScore(int dependents) {
        if (dependents == 0) return 10;
        if (dependents == 1) return 9;
        if (dependents == 2) return 7;
        if (dependents == 3) return 5;
        return 1;
    }

    private String getRiskTier(double score) {
        if (score >= 9.5) return "A+";
        if (score >= 8.5) return "A";
        if (score >= 7.5) return "B+";
        if (score >= 6.5) return "B";
        if (score >= 5.0) return "C";
        return "D";
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}