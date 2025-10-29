package com.lean.lean.dto.webHook;

import com.lean.lean.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetails {
    private String identifier;            // "LEANMB2_SAU"
    private String name;                  // "Lean Mockbank Two"
    private String logo;                  // URL
    private String mainColor;             // "#1beb75"
    private String backgroundColor;       // "#ffffff"
    private AccountType accountType;      // PERSONAL / BUSINESS (per Lean docs; your sample shows PERSONAL)
    private String bankType;
}
