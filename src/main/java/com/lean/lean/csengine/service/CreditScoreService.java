package com.lean.lean.csengine.service;

import java.util.Map;

import com.lean.lean.csengine.dto.ScoreCalculationRequestDTO;

public interface CreditScoreService {
    Map<String, Object> calculateScore(ScoreCalculationRequestDTO request);
}
