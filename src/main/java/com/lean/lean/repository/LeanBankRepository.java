package com.lean.lean.repository;

import com.lean.lean.dao.LeanBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface LeanBankRepository extends JpaRepository<LeanBank, Long> {
    Optional<LeanBank> findByIdentifier(String identifier);
    Optional<LeanBank> findByName(String name);
}