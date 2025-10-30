package com.lean.lean.dto.webHook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankStatusDto {
    private Boolean payments;
    private Boolean data;
}
