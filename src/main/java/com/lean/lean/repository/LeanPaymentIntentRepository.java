package com.lean.lean.repository;


import com.lean.lean.dao.LeanPaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeanPaymentIntentRepository extends JpaRepository<LeanPaymentIntent, Long> {
    Optional<LeanPaymentIntent> findByPaymentIntentId(String paymentIntentId);
    List<LeanPaymentIntent> findByLeanUserId(String leanUserId);
}