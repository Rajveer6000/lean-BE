package com.lean.lean.enums;

public enum PaymentStatus {
    FAILED("FAILED"),
    AWAITING_AUTHORIZATION("AWAITING_AUTHORIZATION"),
    AUTHORIZATION_FAILED("AUTHORIZATION_FAILED"),
    PENDING_WITH_BANK("PENDING_WITH_BANK"),
    ACCEPTED_BY_BANK("ACCEPTED_BY_BANK");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + value);
    }
}
