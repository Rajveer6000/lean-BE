package com.lean.lean.repository;

import com.lean.lean.dao.LeanTransactionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeanTransactionReportRepository extends JpaRepository<LeanTransactionReport, Long> {

    List<LeanTransactionReport> findByUserId(Long userId);

    List<LeanTransactionReport> findByAccountId(String accountId);

    List<LeanTransactionReport> findByUserIdAndAccountId(Long userId, String accountId);
}
