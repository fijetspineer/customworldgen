package com.customworldgen;

import com.customworldgen.config.PresetManager;
import com.customworldgen.config.WorldGenConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomWorldGenMod implements ModInitializer {

    public static final String MOD_ID = "customworldgen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static WorldGenConfig currentConfig;
    private static PresetManager presetManager;

    @Override
    public void onInitialize() {
        currentConfig = WorldGenConfig.createDefault();

        presetManager = new PresetManager();
        presetManager.initialize();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Commands will be registered by the command module
        });

        LOGGER.info("Custom World Gen initialized");
    }

    public static WorldGenConfig getCurrentConfig() {
        return currentConfig;
    }

    public static void setCurrentConfig(WorldGenConfig config) {
        currentConfig = config;
    }

    public static PresetManager getPresetManager() {
        return presetManager;
    }
}
