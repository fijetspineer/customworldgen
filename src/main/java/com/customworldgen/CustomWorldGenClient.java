package com.customworldgen;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomWorldGenClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomWorldGenMod.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Custom World Gen client initialized");
    }
}
