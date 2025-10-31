package com.lean.lean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntentDto {
    private BigDecimal amount;
    private  String currency;
    private  String payment_destination_id;
    private  Long user_id;
    private  String description;
}
