package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.CreditScoreUserCalculationMetaInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditScoreUserCalculationMetaInformationRepository
        extends JpaRepository<CreditScoreUserCalculationMetaInformation, Long> {

    List<CreditScoreUserCalculationMetaInformation> findByUserId(Long userId);

    List<CreditScoreUserCalculationMetaInformation> findByUserCalculationMasterId(Long userCalculationMasterId);

    List<CreditScoreUserCalculationMetaInformation> findByUserCalculationMasterIdAndSource(Long userCalculationMasterId,
            String source);
}
