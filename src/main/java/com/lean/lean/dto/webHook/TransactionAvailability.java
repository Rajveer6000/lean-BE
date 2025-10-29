package com.lean.lean.dto.webHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAvailability {
    private LocalDate start;          // "2025-09-01"
    private LocalDate end;            // "2025-10-29"
    private Integer completeMonths;   // 1
}