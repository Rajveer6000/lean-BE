package com.lean.lean.dto.webHook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentCreatedDto {
    private String id;
    private String customer_id;
    private String intent_id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String payment_destination_id;
    private String debtor_account_id;
    private String bank_transaction_reference;
    private String status_additional_info;
}
