package com.lean.lean.repository;

import com.lean.lean.dao.LeanApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeanApiLogRepository extends JpaRepository<LeanApiLog, Long> {}
