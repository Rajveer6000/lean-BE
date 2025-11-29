package com.lean.lean.csengine.repository;

import com.lean.lean.csengine.dao.EngineConfig;
import com.lean.lean.csengine.enums.EngineConfigStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EngineConfigRepository extends JpaRepository<EngineConfig, Long> {

    Optional<EngineConfig> findByName(String name);

    List<EngineConfig> findByStatus(EngineConfigStatus status);

    Optional<EngineConfig> findByIdAndStatus(Long id, EngineConfigStatus status);
}
