package com.lean.lean.dto.webHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountData {
    private String accountId;
    private String balance;
    private String identity;
    private String transactions;
    private String scheduledPayments;
    private String directDebits;
    private String standingOrders;
    private String beneficiaries;
    private TransactionAvailability transactionAvailability;
}