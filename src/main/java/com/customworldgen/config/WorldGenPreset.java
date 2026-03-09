package com.customworldgen.config;

public class WorldGenPreset {

    private final String name;
    private final String description;
    private final WorldGenConfig config;
    private final long createdTimestamp;

    public WorldGenPreset(String name, String description, WorldGenConfig config) {
        this.name = name;
        this.description = description;
        this.config = config;
        this.createdTimestamp = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public WorldGenConfig getConfig() {
        return config;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }
}
