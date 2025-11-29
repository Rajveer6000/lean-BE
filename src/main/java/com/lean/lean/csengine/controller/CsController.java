package com.lean.lean.csengine.controller;

import com.lean.lean.csengine.service.CreditScoreService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/credit-score")
@AllArgsConstructor
public class CsController {

    @Autowired
    private CreditScoreService creditScoreService;

    @GetMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateScore(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "6") Integer historyMonths) {
        Map<String, Object> result = creditScoreService.calculateScore(userId, historyMonths);
        return ResponseEntity.ok(result);
    }
}
