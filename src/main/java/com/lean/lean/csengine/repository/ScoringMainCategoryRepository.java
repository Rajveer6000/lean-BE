package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.ScoringMainCategory;
import com.lean.lean.csengine.dao.EngineConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoringMainCategoryRepository extends JpaRepository<ScoringMainCategory, Long> {

    List<ScoringMainCategory> findByEngineConfig(EngineConfig engineConfig);

    List<ScoringMainCategory> findByEngineConfigId(Long engineConfigId);

    Optional<ScoringMainCategory> findByEngineConfigIdAndName(Long engineConfigId, String name);

    List<ScoringMainCategory> findByEngineConfigIdOrderByDisplayOrderAsc(Long engineConfigId);
}
