package com.lean.lean.csengine.service;

import java.util.Map;

public interface CreditScoreService {
    Map<String, Object> calculateScore(Long userId, Integer historyMonths);
}
