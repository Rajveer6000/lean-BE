package com.lean.lean.enums;

public enum WebHookType {
    PAYMENT_SOURCE_CREATED("payment_source.created"),
    PAYMENT_SOURCE_BENEFICIARY_CREATED("payment_source.beneficiary.created"),
    PAYMENT_SOURCE_BENEFICIARY_UPDATED("payment_source.beneficiary.updated"),
    PAYMENT_CREATED("payment.created"),
    PAYMENT_UPDATED("payment.updated"),
    ENTITY_CREATED("entity.created"),
    ENTITY_RECONNECTED("entity.reconnected"),
    RESULTS_READY("results.ready"),
    BANK_AVAILABILITY_UPDATED("bank.availability.updated"),
    PAYMENT_RECONCILIATION_UPDATED("payment.reconciliation.updated"),
    PAYMENT_INTENT_CREATED("payment_intent.created");

    private final String value;

    WebHookType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WebHookType fromValue(String value) {
        for (WebHookType type : WebHookType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown webhook type: " + value);
    }
}
