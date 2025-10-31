package com.lean.lean.repository;


import com.lean.lean.dao.LeanPaymentSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LeanPaymentSourceRepository extends JpaRepository<LeanPaymentSource, Long> {
    Optional<LeanPaymentSource> findByPaymentSourceId(String paymentSourceId);
    List<LeanPaymentSource> findByLeanUserId(String leanUserId);
}