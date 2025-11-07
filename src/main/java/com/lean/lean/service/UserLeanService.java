package com.lean.lean.service;

import com.lean.lean.dao.LeanEntity;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.repository.LeanBankRepository;
import com.lean.lean.repository.LeanEntityRepository;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.service.LeanReportService.ReportLink;
import com.lean.lean.util.LeanApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserLeanService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;

    @Autowired
    private LeanEntityRepository leanEntityRepository;

    @Autowired
    private LeanBankRepository leanBankRepository;

    @Autowired
    private LeanApiUtil leanApiUtil;

    @Autowired
    private LeanReportService leanReportService;

    public UserLeanConnectResponse connectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + userId);
        }

        String accessToken = leanApiUtil.getAccessTokenForCustomer(leanUser.getLeanUserId());

        return new UserLeanConnectResponse(leanUser.getLeanUserId(), accessToken);
    }

    public Object getLeanUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("LeanEntity not found"));
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        log.info("accessToken: {}", accessToken);
        return leanApiUtil.getLeanUserDetails(leanEntity.getEntityId(), accessToken);
    }

    public Object getUserAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("LeanEntity not found"));
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        log.info("accessToken: {}", accessToken);
        return leanApiUtil.getUserAccounts(leanEntity.getEntityId(), accessToken);
    }

    public Object getAccountBalances(Long userId, String accountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("LeanEntity not found"));
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        log.info("accessToken: {}", accessToken);
        return leanApiUtil.getAccountBalances(leanEntity.getEntityId(), accountId, accessToken);
    }

    public Object getIncomeDetails(Long userId, LocalDate startDate, String incomeType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("LeanEntity not found"));
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        String normalizedIncomeType = (incomeType == null || incomeType.isBlank())
                ? "ALL"
                : incomeType.trim().toUpperCase();
        String accessToken = leanApiUtil.getAccessToken();
        log.info("Fetching income insights for entityId={} starting {} (type={})",
                leanEntity.getEntityId(), startDate, normalizedIncomeType);
        Object response = leanApiUtil.getIncomeInsights(
                leanEntity.getEntityId(),
                startDate,
                normalizedIncomeType,
                accessToken
        );
        ReportLink reportLink = leanReportService.captureIncomeReport(
                userId,
                startDate,
                normalizedIncomeType,
                response
        );
        return attachReportLink(response, reportLink);
    }

    public Object getExpensesDetails(Long userId, LocalDate startDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("LeanEntity not found"));
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        String accessToken = leanApiUtil.getAccessToken();
        log.info("Fetching expenses insights for entityId={} starting {}",
                leanEntity.getEntityId(), startDate);
        Object response = leanApiUtil.getExpensesInsights(
                leanEntity.getEntityId(),
                startDate,
                accessToken
        );
        ReportLink reportLink = leanReportService.captureExpenseReport(userId, startDate, response);
        return attachReportLink(response, reportLink);
    }

    public Object getNameVerification(Long userId, String fullName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("LeanEntity not found"));
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
        String accessToken = leanApiUtil.getAccessToken();
        log.info("Triggering name verification for entityId={} fullName='{}'",
                leanEntity.getEntityId(), fullName);
        return leanApiUtil.verifyName(
                leanEntity.getEntityId(),
                fullName,
                accessToken
        );
    }
    public Object getuserPaymentSources(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.getPaymentSources(
                leanUser.getLeanUserId(),
                accessToken
        );
    }


    public Object getUserTransactions(Long userId, String accountId, LocalDate fromDate, LocalDate toDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        LeanEntity leanEntity = leanEntityRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("LeanEntity not found"));
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + user.getId());
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be on or before toDate");
        }
        String accessToken = leanApiUtil.getAccessToken();
        log.info("Fetching transactions for entityId={} accountId={} range=[{} - {}]",
                leanEntity.getEntityId(), accountId, fromDate, toDate);
        Object response = leanApiUtil.getUserTransactions(
                leanEntity.getEntityId(),
                accountId,
                fromDate,
                toDate,
                accessToken
        );
        Object filtered = applyDateFilters(response, fromDate, toDate);
        ReportLink reportLink = leanReportService.captureTransactionReport(
                userId,
                accountId,
                fromDate,
                toDate,
                filtered
        );
        return attachReportLink(filtered, reportLink);
    }

    private Object attachReportLink(Object response, ReportLink reportLink) {
        if (reportLink == null || !(response instanceof Map<?, ?>)) {
            return response;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = new LinkedHashMap<>((Map<String, Object>) response);
        responseMap.put("report_file_path", reportLink.objectKey());
        if (reportLink.signedUrl() != null) {
            responseMap.put("report_download_url", reportLink.signedUrl());
        }
        return responseMap;
    }

    private Object applyDateFilters(Object response, LocalDate fromDate, LocalDate toDate) {
        if (response == null || (fromDate == null && toDate == null)) {
            return response;
        }
        if (!(response instanceof Map)) {
            return response;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = new LinkedHashMap<>((Map<String, Object>) response);
        boolean updated = filterTransactionsInMap(responseMap, fromDate, toDate);
        return updated ? responseMap : response;
    }


    @SuppressWarnings("unchecked")
    private boolean filterTransactionsInMap(Map<String, Object> map, LocalDate fromDate, LocalDate toDate) {
        boolean updated = false;

        Object transactionsObj = map.get("transactions");
        if (transactionsObj instanceof List<?>) {
            List<Object> filtered = filterTransactionsList((List<?>) transactionsObj, fromDate, toDate);
            if (filtered != null) {
                map.put("transactions", filtered);
                updated = true;
            }
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                Map<String, Object> nestedCopy = new LinkedHashMap<>((Map<String, Object>) value);
                if (filterTransactionsInMap(nestedCopy, fromDate, toDate)) {
                    map.put(entry.getKey(), nestedCopy);
                    updated = true;
                }
            } else if (value instanceof List<?>) {
                List<Object> processed = processNestedList((List<?>) value, fromDate, toDate);
                if (processed != null) {
                    map.put(entry.getKey(), processed);
                    updated = true;
                }
            }
        }

        return updated;
    }

    private List<Object> processNestedList(List<?> list, LocalDate fromDate, LocalDate toDate) {
        boolean updated = false;
        List<Object> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedCopy = new LinkedHashMap<>((Map<String, Object>) item);
                if (filterTransactionsInMap(nestedCopy, fromDate, toDate)) {
                    updated = true;
                }
                result.add(nestedCopy);
            } else if (item instanceof List<?>) {
                List<Object> nestedList = processNestedList((List<?>) item, fromDate, toDate);
                if (nestedList != null) {
                    updated = true;
                    result.add(nestedList);
                } else {
                    result.add(item);
                }
            } else {
                result.add(item);
            }
        }
        return updated ? result : null;
    }

    private List<Object> filterTransactionsList(List<?> transactions, LocalDate fromDate, LocalDate toDate) {
        if (transactions.isEmpty()) {
            return null;
        }
        boolean changed = false;
        List<Object> filtered = new ArrayList<>(transactions.size());
        for (Object transaction : transactions) {
            LocalDate transactionDate = extractTransactionDate(transaction);
            if (transactionDate == null) {
                filtered.add(transaction);
                continue;
            }
            if (fromDate != null && transactionDate.isBefore(fromDate)) {
                changed = true;
                continue;
            }
            if (toDate != null && transactionDate.isAfter(toDate)) {
                changed = true;
                continue;
            }
            filtered.add(transaction);
        }
        return changed ? filtered : null;
    }

    private LocalDate extractTransactionDate(Object transaction) {
        if (!(transaction instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> txnMap = (Map<?, ?>) transaction;
        String[] candidateKeys = new String[]{
                "date",
                "transaction_date",
                "transactionDate",
                "timestamp",
                "posted_at",
                "posting_date",
                "value_date"
        };
        for (String key : candidateKeys) {
            if (txnMap.containsKey(key)) {
                LocalDate parsed = parseDateValue(txnMap.get(key));
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        return null;
    }

    private LocalDate parseDateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return parseDateString((String) value);
        }
        if (value instanceof Map<?, ?>) {
            Map<?, ?> nested = (Map<?, ?>) value;
            for (Object nestedValue : nested.values()) {
                LocalDate parsed = parseDateValue(nestedValue);
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        return null;
    }

    private LocalDate parseDateString(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(trimmed).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(trimmed).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
