package com.lean.lean.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanEntity;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.enums.PaymentIntentStatus;
import com.lean.lean.enums.ProofOfAddressDocumentType;
import com.lean.lean.repository.LeanBankRepository;
import com.lean.lean.repository.LeanEntityRepository;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.service.LeanReportService.ReportLink;
import com.lean.lean.util.LeanApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class UserLeanService {

    private static final String DEFAULT_DELETE_REASON = "USER_REQUESTED";
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public Object deletePaymentSource(Long userId, String paymentSourceId, String reason) {
        if (paymentSourceId == null || paymentSourceId.isBlank()) {
            throw new IllegalArgumentException("paymentSourceId is required");
        }
        LeanUser leanUser = requireLeanUser(userId);
        String resolvedReason = (reason == null || reason.isBlank()) ? DEFAULT_DELETE_REASON : reason;
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.deletePaymentSource(
                leanUser.getLeanUserId(),
                paymentSourceId,
                resolvedReason,
                accessToken
        );
    }

    public Object getPaymentDetails(Long userId, String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId is required");
        }
        requireLeanUser(userId);
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.getPaymentById(paymentId, accessToken);
    }

    public Object listPaymentIntents(Long userId,
                                     Integer page,
                                     Integer size,
                                     LocalDate from,
                                     LocalDate to,
                                     PaymentIntentStatus status) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to when both are provided");
        }
        LeanUser leanUser = requireLeanUser(userId);
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.listPaymentIntents(
                accessToken,
                leanUser.getLeanUserId(),
                page,
                size,
                from,
                to,
                status
        );
    }

    public Object getPaymentIntentDetails(Long userId, String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new IllegalArgumentException("paymentIntentId is required");
        }
        requireLeanUser(userId);
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.getPaymentIntentById(paymentIntentId, accessToken);
    }

    public Object getCustomerEntityDetails(Long userId, String entityId) {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("entityId is required");
        }
        LeanUser leanUser = requireLeanUser(userId);
        validateEntityOwnership(userId, entityId);
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.getCustomerEntity(
                accessToken,
                leanUser.getLeanUserId(),
                entityId
        );
    }

    public Object deleteCustomerEntity(Long userId, String entityId, String reason) {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("entityId is required");
        }
        LeanUser leanUser = requireLeanUser(userId);
        validateEntityOwnership(userId, entityId);
        String resolvedReason = (reason == null || reason.isBlank()) ? DEFAULT_DELETE_REASON : reason;
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.deleteCustomerEntity(
                accessToken,
                leanUser.getLeanUserId(),
                entityId,
                resolvedReason
        );
    }

    public Object uploadProofOfAddress(Long userId,
                                       ProofOfAddressDocumentType documentType,
                                       String fullName,
                                       String referenceDataJson,
                                       MultipartFile file) {
        if (documentType == null) {
            throw new IllegalArgumentException("documentType is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        Map<String, Object> referenceData = parseReferenceData(referenceDataJson);
        LeanUser leanUser = requireLeanUser(userId);
        String accessToken = leanApiUtil.getAccessToken();
        return leanApiUtil.uploadProofOfAddress(
                accessToken,
                leanUser.getLeanUserId(),
                documentType,
                fullName,
                referenceData,
                file
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
        ReportLink reportLink = leanReportService.captureTransactionReport(
                userId,
                accountId,
                fromDate,
                toDate,
                response
        );
        return attachReportLink(response, reportLink);
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

    private LeanUser requireLeanUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LeanUser leanUser = leanUserRepository.findFirstByUserId(userId);
        if (leanUser == null) {
            throw new RuntimeException("LeanUser not found for user ID: " + userId);
        }
        return leanUser;
    }

    private void validateEntityOwnership(Long userId, String entityId) {
        leanEntityRepository.findByEntityId(entityId).ifPresent(entity -> {
            String ownerId = entity.getUserId();
            if (ownerId != null && !ownerId.equals(userId.toString())) {
                throw new RuntimeException("Entity does not belong to user ID: " + userId);
            }
        });
    }

    private Map<String, Object> parseReferenceData(String referenceDataJson) {
        if (referenceDataJson == null || referenceDataJson.isBlank()) {
            throw new IllegalArgumentException("reference_data is required");
        }
        try {
            return objectMapper.readValue(referenceDataJson, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("reference_data must be a valid JSON object", e);
        }
    }

}
