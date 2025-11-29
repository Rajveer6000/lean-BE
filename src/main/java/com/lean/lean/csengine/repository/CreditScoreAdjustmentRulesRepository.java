package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.CreditScoreAdjustmentRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditScoreAdjustmentRulesRepository extends JpaRepository<CreditScoreAdjustmentRules, Long> {

    List<CreditScoreAdjustmentRules> findByEngineConfigId(Long engineConfigId);

    List<CreditScoreAdjustmentRules> findByEngineConfigIdOrderByDisplayOrderAsc(Long engineConfigId);
}
