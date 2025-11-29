package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.AdjustmentRulesCalculations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdjustmentRulesCalculationsRepository extends JpaRepository<AdjustmentRulesCalculations, Long> {

    List<AdjustmentRulesCalculations> findByUserCalculationMasterId(Long userCalculationMasterId);

    List<AdjustmentRulesCalculations> findByUserCalculationMasterIdAndConditionMet(Long userCalculationMasterId,
            Boolean conditionMet);

    List<AdjustmentRulesCalculations> findByBonusPenaltyId(Long bonusPenaltyId);
}
