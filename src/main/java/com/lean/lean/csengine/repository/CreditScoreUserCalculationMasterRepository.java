package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.CreditScoreUserCalculationMaster;
import com.lean.lean.csengine.enums.CalculationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditScoreUserCalculationMasterRepository
        extends JpaRepository<CreditScoreUserCalculationMaster, Long> {

    List<CreditScoreUserCalculationMaster> findByUserId(Long userId);

    List<CreditScoreUserCalculationMaster> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CreditScoreUserCalculationMaster> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    List<CreditScoreUserCalculationMaster> findByCalculationStatus(CalculationStatus status);

    List<CreditScoreUserCalculationMaster> findByEngineConfigId(Long engineConfigId);

    List<CreditScoreUserCalculationMaster> findByUserIdAndEngineConfigId(Long userId, Long engineConfigId);

    List<CreditScoreUserCalculationMaster> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
