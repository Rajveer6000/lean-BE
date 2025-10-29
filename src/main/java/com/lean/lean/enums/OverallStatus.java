package com.lean.lean.enums;

public enum OverallStatus {
    PENDING("PENDING"),
    OK("OK"),
    FAILED("FAILED"),
    UNSUPPORTED("UNSUPPORTED");

    private final String value;

    OverallStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OverallStatus fromValue(String value) {
        for (OverallStatus status : OverallStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown overall status: " + value);
    }
}
