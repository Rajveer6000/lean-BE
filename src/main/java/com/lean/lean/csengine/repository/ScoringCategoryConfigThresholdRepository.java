package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.ScoringCategoryConfigThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoringCategoryConfigThresholdRepository extends JpaRepository<ScoringCategoryConfigThreshold, Long> {

    List<ScoringCategoryConfigThreshold> findByCategoryConfigMasterId(Long categoryConfigMasterId);

    List<ScoringCategoryConfigThreshold> findByCategoryConfigMasterIdIn(List<Long> categoryConfigMasterIds);

}
