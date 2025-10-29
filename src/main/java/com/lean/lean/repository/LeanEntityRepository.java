package com.lean.lean.repository;

import com.lean.lean.dao.LeanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LeanEntityRepository extends JpaRepository<LeanEntity, Long> {
    Optional<LeanEntity> findByEntityId(String entityId);
    List<LeanEntity> findByUserId(String userId);
    List<LeanEntity> findByBankId(String bankId);
}
