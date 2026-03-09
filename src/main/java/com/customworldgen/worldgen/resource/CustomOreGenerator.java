package com.customworldgen.worldgen.resource;

import com.customworldgen.config.WorldGenConfig;

/**
 * Utility class for adjusting ore generation parameters based on a {@link WorldGenConfig.ResourceConfig}.
 */
public final class CustomOreGenerator {

    private CustomOreGenerator() {
    }

    /**
     * Returns the adjusted ore vein count for the given ore type, taking into account
     * the frequency multiplier and the scarcity preset.
     */
    public static int getAdjustedOreCount(String oreType, WorldGenConfig.ResourceConfig config) {
        int baseCount = getBaseOreCount(oreType);
        float scarcityMultiplier = getScarcityMultiplier(config.getScarcityPreset());
        return Math.max(1, Math.round(baseCount * config.getOreFrequency() * scarcityMultiplier));
    }

    /**
     * Returns the configured minimum Y-level for ore generation.
     */
    public static int getOreMinHeight(WorldGenConfig.ResourceConfig config) {
        return config.getOreMinHeight();
    }

    /**
     * Returns the configured maximum Y-level for ore generation.
     */
    public static int getOreMaxHeight(WorldGenConfig.ResourceConfig config) {
        return config.getOreMaxHeight();
    }

    /**
     * Returns the cluster size multiplier derived from the config's cluster size setting.
     */
    public static float getClusterMultiplier(WorldGenConfig.ResourceConfig config) {
        return Math.max(0.1f, config.getClusterSize());
    }

    // ---- Internal helpers ----

    private static int getBaseOreCount(String oreType) {
        if (oreType == null) {
            return 8;
        }
        return switch (oreType.toLowerCase()) {
            case "diamond" -> 4;
            case "gold" -> 8;
            case "iron" -> 12;
            case "copper" -> 16;
            case "coal" -> 20;
            case "lapis" -> 6;
            case "redstone" -> 8;
            case "emerald" -> 2;
            default -> 8;
        };
    }

    private static float getScarcityMultiplier(String preset) {
        if (preset == null) {
            return 1.0f;
        }
        return switch (preset.toLowerCase()) {
            case "abundant" -> 1.5f;
            case "rich" -> 1.25f;
            case "normal" -> 1.0f;
            case "scarce" -> 0.7f;
            case "barren" -> 0.4f;
            default -> 1.0f;
        };
    }
}
