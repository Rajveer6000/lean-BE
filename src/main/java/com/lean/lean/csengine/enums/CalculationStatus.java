package com.lean.lean.csengine.enums;

public enum CalculationStatus {
    PENDING(0),
    IN_PROGRESS(1),
    COMPLETED(2),
    FAILED(3);

    private final Integer value;

    CalculationStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static CalculationStatus fromValue(Integer value) {
        for (CalculationStatus status : CalculationStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown calculation status: " + value);
    }
}
