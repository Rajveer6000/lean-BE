package com.lean.lean.repository;

import com.lean.lean.dao.LeanDestinations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationsBeneficiaryRepository extends JpaRepository<LeanDestinations, Long> {
    LeanDestinations findByUuid(String uuid);

    @Query(value = "SELECT * FROM destinations_beneficiary WHERE bank_identifier = :bankIdentifier AND account_number = :accountNumber LIMIT 1", nativeQuery = true)
    LeanDestinations findByIdentifierAndAccountNumber(@Param("bankIdentifier") String bankIdentifier, @Param("accountNumber") String accountNumber);

}
