package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.CategoryCalculations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryCalculationsRepository extends JpaRepository<CategoryCalculations, Long> {

    List<CategoryCalculations> findByUserCalculationMasterId(Long userCalculationMasterId);

    List<CategoryCalculations> findByMainCategoryId(Long mainCategoryId);
}
