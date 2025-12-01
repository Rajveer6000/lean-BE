package com.lean.lean.csengine.controller;

import com.lean.lean.csengine.service.CreditScoreService;
import lombok.AllArgsConstructor;
import com.lean.lean.csengine.dto.ScoreCalculationRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/credit-score")
@AllArgsConstructor
public class CsController {

    @Autowired
    private CreditScoreService creditScoreService;

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateScore(
            @RequestBody ScoreCalculationRequestDTO request) {
        Map<String, Object> result = creditScoreService.calculateScore(request);
        return ResponseEntity.ok(result);
    }
}
