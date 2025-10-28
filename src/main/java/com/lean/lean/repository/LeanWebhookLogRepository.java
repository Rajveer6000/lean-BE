package com.lean.lean.repository;

import com.lean.lean.dao.LeanWebhookLog;
import com.lean.lean.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeanWebhookLogRepository extends JpaRepository<LeanWebhookLog, Long> {
}
