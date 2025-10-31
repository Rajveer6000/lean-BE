package com.lean.lean.repository;

import com.lean.lean.dao.LeanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeanPaymentRepository extends JpaRepository<LeanPayment, Long> {
    Optional<LeanPayment> findByPaymentId(String paymentId);
    List<LeanPayment> findByLeanUserId(String leanUserId);
}