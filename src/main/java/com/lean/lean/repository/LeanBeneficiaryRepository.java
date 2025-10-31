package com.lean.lean.repository;

import com.lean.lean.dao.LeanBeneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeanBeneficiaryRepository extends JpaRepository<LeanBeneficiary, Long> {
    Optional<LeanBeneficiary> findByBeneficiaryId(String beneficiaryId);
    List<LeanBeneficiary> findByLeanUserId(String leanUserId);
    Optional<LeanBeneficiary> findByPaymentSourceIdAndPaymentDestinationId(String sourceId, String destinationId);
}