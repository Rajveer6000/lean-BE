package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.CreditScoreAdjustmentRulesConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditScoreAdjustmentRulesConfigRepository
        extends JpaRepository<CreditScoreAdjustmentRulesConfig, Long> {

    List<CreditScoreAdjustmentRulesConfig> findByAdjustmentRulesId(Long adjustmentRulesId);
}
