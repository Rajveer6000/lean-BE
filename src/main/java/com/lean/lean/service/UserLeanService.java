package com.lean.lean.service;

import com.lean.lean.dao.LeanEntity;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.UserLeanConnectResponse;
import com.lean.lean.repository.LeanBankRepository;
import com.lean.lean.repository.LeanEntityRepository;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.util.LeanApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
        return leanApiUtil.getIncomeInsights(
                leanEntity.getEntityId(),
                startDate,
                normalizedIncomeType,
                accessToken
        );
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
        return leanApiUtil.getExpensesInsights(
                leanEntity.getEntityId(),
                startDate,
                accessToken
        );
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


    public Object getUserTransactions(Long userId, String accountId, LocalDate fromDate, LocalDate toDate) {
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
        return leanApiUtil.getUserTransactions(leanEntity.getEntityId(), accountId, fromDate, toDate, accessToken);
    }
}
