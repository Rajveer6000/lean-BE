package com.lean.lean.enums;


public enum Permission {
    IDENTITY("identity"),
    ACCOUNTS("accounts"),
    BALANCE("balance"),
    TRANSACTIONS("transactions"),
    IDENTITIES("identities"),
    STANDING_ORDERS("standing_orders"),
    SCHEDULED_PAYMENTS("scheduled_payments"),
    DIRECT_DEBITS("direct_debits"),
    BENEFICIARIES("beneficiaries");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Permission fromValue(String value) {
        for (Permission permission : Permission.values()) {
            if (permission.value.equalsIgnoreCase(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + value);
    }
}