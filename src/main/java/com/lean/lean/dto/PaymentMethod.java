package com.lean.lean.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentMethod {

    private String cardNumber;
    private String cardHolderName;
    private String expirationDate;
    private String cvv;
}
