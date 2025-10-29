package com.lean.lean.enums;

public enum PaymentMethodType {
    CREDIT_CARD("CREDIT_CARD"),
    DEBIT_CARD("DEBIT_CARD"),
    WALLET("WALLET");

    private final String value;

    PaymentMethodType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentMethodType fromValue(String value) {
        for (PaymentMethodType type : PaymentMethodType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown payment method type: " + value);
    }
}
