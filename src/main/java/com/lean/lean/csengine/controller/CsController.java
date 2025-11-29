package com.lean.lean.csengine.controller;

import com.lean.lean.csengine.dto.RentSavvyScoreInputDTO;
import com.lean.lean.csengine.service.CreditScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/credit-score")
public class CsController {

    @Autowired
    private CreditScoreService creditScoreService;

    @GetMapping("/{id}")
    public RentSavvyScoreInputDTO calculateScore(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "3") Integer historyMonths) {
        if (historyMonths < 3) {
            throw new IllegalArgumentException("Minimum 3 months of history required for Rent-Savvy scoring.");
        }

        return creditScoreService.calculateScore(id, historyMonths);
    }
}
