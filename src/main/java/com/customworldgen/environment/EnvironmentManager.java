package com.customworldgen.environment;

import com.customworldgen.CustomWorldGenMod;
import com.customworldgen.config.WorldGenConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

/**
 * Manages environmental rules such as day/night cycle length, weather, gravity, and ocean type
 * based on a {@link WorldGenConfig.EnvironmentConfig}.
 */
public class EnvironmentManager {

    private final WorldGenConfig.EnvironmentConfig config;

    public EnvironmentManager(WorldGenConfig.EnvironmentConfig config) {
        this.config = config;
    }

    /**
     * Applies environmental rules to all worlds on the server.
     * Adjusts the day/night cycle length and weather behaviour.
     */
    public void applyEnvironmentRules(MinecraftServer server) {
        int dayLength = getAdjustedDayLength();

        for (ServerWorld world : server.getWorlds()) {
            // Adjust the time-of-day progression by warping the time based on
            // the ratio between vanilla day length and the configured value
            if (dayLength != 24000) {
                long currentTime = world.getTimeOfDay();
                float timeScale = 24000.0f / dayLength;
                long adjustedTime = (long) (currentTime * timeScale) % dayLength;
                world.setTimeOfDay(adjustedTime);
            }

            // Weather control
            if (config.getWeatherFrequency() <= 0.0f) {
                world.setWeather(0, 0, false, false);
            }
        }

        CustomWorldGenMod.LOGGER.debug("Applied environment rules: dayLength={}, weatherFreq={}, lavaOceans={}, gravity={}",
                dayLength, getWeatherChance(), shouldUseLavaOceans(), getGravityMultiplier());
    }

    /**
     * Returns the configured day length in ticks. Clamped to a minimum of 1000 ticks.
     */
    public int getAdjustedDayLength() {
        return Math.max(1000, config.getDayLengthTicks());
    }

    /**
     * Returns the weather frequency as a probability (0.0 = never, 1.0 = vanilla rate).
     */
    public float getWeatherChance() {
        return Math.max(0.0f, Math.min(1.0f, config.getWeatherFrequency()));
    }

    /**
     * Returns whether oceans should be filled with lava instead of water.
     */
    public boolean shouldUseLavaOceans() {
        return config.isLavaOceans();
    }

    /**
     * Returns the gravity multiplier (1.0 = vanilla gravity).
     */
    public float getGravityMultiplier() {
        return Math.max(0.0f, config.getGravityMultiplier());
    }
}
