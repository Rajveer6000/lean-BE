package com.lean.lean.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentIntentDTO {
    @JsonProperty("payment_destination")
    private PaymentDestinationDTO paymentDestination;
    private BigDecimal amount;
    @JsonProperty("payment_intent_id")
    private String paymentIntentId;
    private String updatedAt;
    private List<Object> payments;
    private String description;
    private String createdAt;
    private String currency;
    @JsonProperty("customer_id")
    private String customerId;
}