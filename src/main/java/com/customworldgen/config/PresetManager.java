package com.customworldgen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PresetManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("customworldgen");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path presetsDir;

    public PresetManager() {
        this.presetsDir = FabricLoader.getInstance().getConfigDir()
                .resolve("customworldgen")
                .resolve("presets");
    }

    public void initialize() {
        try {
            Files.createDirectories(presetsDir);
            createBuiltInPresets();
            LOGGER.info("PresetManager initialized – presets directory: {}", presetsDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create presets directory", e);
        }
    }

    public WorldGenPreset loadPreset(String name) {
        Path file = presetsDir.resolve(sanitizeFileName(name) + ".json");
        if (!Files.exists(file)) {
            LOGGER.warn("Preset file not found: {}", file);
            return null;
        }
        try {
            String json = Files.readString(file);
            return GSON.fromJson(json, WorldGenPreset.class);
        } catch (IOException e) {
            LOGGER.error("Failed to load preset '{}'", name, e);
            return null;
        }
    }

    public void savePreset(WorldGenPreset preset) {
        Path file = presetsDir.resolve(sanitizeFileName(preset.getName()) + ".json");
        try {
            Files.writeString(file, GSON.toJson(preset));
            LOGGER.info("Saved preset '{}'", preset.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to save preset '{}'", preset.getName(), e);
        }
    }

    public void deletePreset(String name) {
        Path file = presetsDir.resolve(sanitizeFileName(name) + ".json");
        try {
            if (Files.deleteIfExists(file)) {
                LOGGER.info("Deleted preset '{}'", name);
            } else {
                LOGGER.warn("Preset '{}' does not exist", name);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to delete preset '{}'", name, e);
        }
    }

    public List<WorldGenPreset> listPresets() {
        List<WorldGenPreset> presets = new ArrayList<>();
        if (!Files.isDirectory(presetsDir)) {
            return presets;
        }
        try (Stream<Path> paths = Files.list(presetsDir)) {
            paths.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try {
                    String json = Files.readString(p);
                    WorldGenPreset preset = GSON.fromJson(json, WorldGenPreset.class);
                    if (preset != null) {
                        presets.add(preset);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read preset file: {}", p, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list presets", e);
        }
        return presets;
    }

    public String exportPresetJson(WorldGenPreset preset) {
        return GSON.toJson(preset);
    }

    public WorldGenPreset importPresetJson(String json) {
        return GSON.fromJson(json, WorldGenPreset.class);
    }

    public void createBuiltInPresets() {
        // Default preset
        if (!Files.exists(presetsDir.resolve("default.json"))) {
            savePreset(new WorldGenPreset(
                    "default",
                    "Standard world generation with vanilla-like settings",
                    WorldGenConfig.createDefault()
            ));
        }

        // Amplified preset
        if (!Files.exists(presetsDir.resolve("amplified.json"))) {
            WorldGenConfig amplified = WorldGenConfig.createDefault();
            amplified.getAdvanced().setAmplifiedMode(true);
            amplified.getAdvanced().setAmplifiedScale(2.0f);
            amplified.getTerrain().setMountainHeight(2.5f);
            amplified.getTerrain().setTerrainScale(1.5f);
            savePreset(new WorldGenPreset(
                    "amplified",
                    "Amplified terrain with extreme mountain heights",
                    amplified
            ));
        }

        // Skylands preset
        if (!Files.exists(presetsDir.resolve("skylands.json"))) {
            WorldGenConfig skylands = WorldGenConfig.createDefault();
            skylands.getAdvanced().setSkylandsMode(true);
            skylands.getTerrain().setFloatingIslands(true);
            skylands.getTerrain().setFloatingIslandDensity(0.7f);
            skylands.getTerrain().setBaseHeight(96);
            skylands.getEnvironment().setOceanSize(0.0f);
            savePreset(new WorldGenPreset(
                    "skylands",
                    "Floating islands in the sky with no ocean",
                    skylands
            ));
        }

        // Cave world preset
        if (!Files.exists(presetsDir.resolve("cave_world.json"))) {
            WorldGenConfig caveWorld = WorldGenConfig.createDefault();
            caveWorld.getTerrain().setCaveDensity(2.0f);
            caveWorld.getTerrain().setCaveType(CaveType.CHEESE);
            caveWorld.getTerrain().setBaseHeight(128);
            caveWorld.getTerrain().setUndergroundBiomes(true);
            caveWorld.getResource().setOreFrequency(1.5f);
            savePreset(new WorldGenPreset(
                    "cave_world",
                    "Massive cave networks with abundant underground resources",
                    caveWorld
            ));
        }
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }
}
