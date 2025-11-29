package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.ScoringCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoringCategoryTypeRepository extends JpaRepository<ScoringCategoryType, Long> {

    Optional<ScoringCategoryType> findByName(String name);
}
