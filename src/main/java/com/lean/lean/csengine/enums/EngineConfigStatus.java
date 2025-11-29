package com.lean.lean.csengine.enums;

public enum EngineConfigStatus {
    INACTIVE(0),
    ACTIVE(1),
    DELETED(2);

    private final Integer value;

    EngineConfigStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static EngineConfigStatus fromValue(Integer value) {
        for (EngineConfigStatus status : EngineConfigStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown engine config status: " + value);
    }
}
