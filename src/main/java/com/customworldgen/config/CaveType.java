package com.customworldgen.config;

public enum CaveType {
    NORMAL("Normal"),
    SPAGHETTI("Spaghetti"),
    CHEESE("Cheese"),
    NOODLE("Noodle");

    private final String displayName;

    CaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CaveType fromString(String name) {
        for (CaveType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return NORMAL;
    }
}
