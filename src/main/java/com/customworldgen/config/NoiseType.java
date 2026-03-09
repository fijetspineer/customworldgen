package com.customworldgen.config;

public enum NoiseType {
    PERLIN("Perlin"),
    SIMPLEX("Simplex"),
    RIDGED("Ridged"),
    VORONOI("Voronoi");

    private final String displayName;

    NoiseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static NoiseType fromString(String name) {
        for (NoiseType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return PERLIN;
    }
}
