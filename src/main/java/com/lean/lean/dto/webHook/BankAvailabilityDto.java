package com.lean.lean.dto.webHook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAvailabilityDto {
    private String identifier;
    private AvailabilityDto availability;
}
