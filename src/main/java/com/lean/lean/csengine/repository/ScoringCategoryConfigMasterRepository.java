package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.ScoringCategoryConfigMaster;
import com.lean.lean.csengine.enums.EngineConfigStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoringCategoryConfigMasterRepository extends JpaRepository<ScoringCategoryConfigMaster, Long> {

    List<ScoringCategoryConfigMaster> findByEngineConfigId(Long engineConfigId);

    List<ScoringCategoryConfigMaster> findByMainCategoryId(Long mainCategoryId);

    List<ScoringCategoryConfigMaster> findByEngineConfigIdAndStatus(Long engineConfigId, EngineConfigStatus status);

    List<ScoringCategoryConfigMaster> findByMainCategoryIdAndStatus(Long mainCategoryId, EngineConfigStatus status);
}
