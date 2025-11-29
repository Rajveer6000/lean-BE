package com.lean.lean.csengine.service;

import com.lean.lean.csengine.dto.RentSavvyScoreInputDTO;

public interface CreditScoreService  {
    RentSavvyScoreInputDTO calculateScore(Long userId, Integer historyMonths);
}
