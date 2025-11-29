package com.lean.lean.csengine.enums;

public enum LogLevel {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    private final String value;

    LogLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LogLevel fromValue(String value) {
        for (LogLevel level : LogLevel.values()) {
            if (level.value.equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown log level: " + value);
    }
}
