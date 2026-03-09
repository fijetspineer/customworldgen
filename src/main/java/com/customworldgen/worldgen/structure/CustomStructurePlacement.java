package com.customworldgen.worldgen.structure;

import com.customworldgen.config.WorldGenConfig;

import java.util.Random;

/**
 * Utility class that provides methods to modify structure generation rates and placement.
 */
public final class CustomStructurePlacement {

    private CustomStructurePlacement() {
    }

    /**
     * Determines whether a structure should generate at a given location based on
     * the configured rates and overall structure density.
     */
    public static boolean shouldGenerateStructure(String structureId, WorldGenConfig.StructureConfig config, Random random) {
        float rate = getStructureRate(structureId, config);
        float effectiveRate = rate * config.getStructureDensity();
        return random.nextFloat() < effectiveRate;
    }

    /**
     * Returns the minimum distance (in chunks) between instances of the given structure type.
     */
    public static int getMinDistance(String structureId, WorldGenConfig.StructureConfig config) {
        int baseDistance = config.getMinStructureDistance();
        float rate = getStructureRate(structureId, config);

        if (rate <= 0.0f) {
            return Integer.MAX_VALUE;
        }

        // Higher rates reduce the minimum distance; lower rates increase it
        return Math.max(1, Math.round(baseDistance / rate));
    }

    /**
     * Returns the configured generation rate for a specific structure type.
     * Falls back to structure density for unknown structure IDs.
     */
    public static float getStructureRate(String structureId, WorldGenConfig.StructureConfig config) {
        if (structureId == null) {
            return config.getStructureDensity();
        }

        String id = structureId.toLowerCase();

        if (id.contains("village")) {
            return config.getVillageRate();
        } else if (id.contains("stronghold")) {
            return config.getStrongholdRate();
        } else if (id.contains("mineshaft")) {
            return config.getMineshaftRate();
        } else if (id.contains("temple") || id.contains("monument") || id.contains("mansion")) {
            return config.getTempleRate();
        } else if (id.contains("ancient_city")) {
            return config.getAncientCityRate();
        }

        return config.getStructureDensity();
    }
}
