package com.lean.lean.enums;

public enum AccountType {
    CURRENT("CURRENT"),
    SAVINGS("SAVINGS"),
    CREDIT("CREDIT");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AccountType fromValue(String value) {
        for (AccountType type : AccountType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown account type: " + value);
    }
}
