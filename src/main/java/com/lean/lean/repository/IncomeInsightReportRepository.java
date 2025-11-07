package com.lean.lean.repository;

import com.lean.lean.dao.IncomeInsightReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomeInsightReportRepository extends JpaRepository<IncomeInsightReport, Long> {

    List<IncomeInsightReport> findByUserId(Long userId);

    List<IncomeInsightReport> findByUserIdAndIncomeType(Long userId, String incomeType);
}
