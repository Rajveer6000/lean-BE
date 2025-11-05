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
public class PaymentUpdatedDto {
    private String id;
    private BigDecimal amount;
    private String status;
    private String intent_id;
    private String customer_id;
    private String end_user_id;
    private String post_initiation_status;
    private String bank_transaction_reference;
    private String currency;
    private String payment_destination_id;
    private String debtor_account_id;
    private String failure_reason;
    private String failure_code;
    private String status_additional_info;
}
