package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.CreditScoreUserCalculationLogs;
import com.lean.lean.csengine.enums.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditScoreUserCalculationLogsRepository extends JpaRepository<CreditScoreUserCalculationLogs, Long> {

    List<CreditScoreUserCalculationLogs> findByUserCalculationMasterId(Long userCalculationMasterId);

    List<CreditScoreUserCalculationLogs> findByUserId(Long userId);

    List<CreditScoreUserCalculationLogs> findByUserCalculationMasterIdAndLogLevel(Long userCalculationMasterId,
            LogLevel logLevel);

    List<CreditScoreUserCalculationLogs> findByUserIdOrderByCreatedAtDesc(Long userId);
}
