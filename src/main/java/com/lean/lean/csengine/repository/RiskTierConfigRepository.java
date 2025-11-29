package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.RiskTierConfig;
import com.lean.lean.csengine.enums.EngineConfigStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskTierConfigRepository extends JpaRepository<RiskTierConfig, Long> {

    List<RiskTierConfig> findByEngineConfigId(Long engineConfigId);

    List<RiskTierConfig> findByEngineConfigIdAndStatus(Long engineConfigId, EngineConfigStatus status);

    Optional<RiskTierConfig> findByEngineConfigIdAndRiskTier(Long engineConfigId, String riskTier);

    Optional<RiskTierConfig> findByEngineConfigIdAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(
            Long engineConfigId, BigDecimal score1, BigDecimal score2);
}
