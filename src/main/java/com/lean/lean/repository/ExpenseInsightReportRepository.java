package com.lean.lean.repository;

import com.lean.lean.dao.ExpenseInsightReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseInsightReportRepository extends JpaRepository<ExpenseInsightReport, Long> {

    List<ExpenseInsightReport> findByUserId(Long userId);
}
