package com.customworldgen.worldgen.biome;

import com.customworldgen.config.WorldGenConfig;
import com.customworldgen.noise.NoiseGenerator;
import com.customworldgen.config.NoiseType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A custom biome source that selects biomes based on noise-driven temperature and humidity
 * values, configured through {@link WorldGenConfig.BiomeConfig}.
 */
public class CustomBiomeSource extends BiomeSource {

    public static final MapCodec<CustomBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("biome_size", 4).forGetter(s -> s.biomeConfig.getBiomeSize()),
                    Codec.FLOAT.optionalFieldOf("temperature_offset", 0.0f).forGetter(s -> s.biomeConfig.getTemperatureOffset()),
                    Codec.FLOAT.optionalFieldOf("humidity_offset", 0.0f).forGetter(s -> s.biomeConfig.getHumidityOffset()),
                    Codec.FLOAT.optionalFieldOf("biome_blending", 1.0f).forGetter(s -> s.biomeConfig.getBiomeBlending())
            ).apply(instance, (biomeSize, tempOffset, humOffset, blending) -> {
                WorldGenConfig.BiomeConfig cfg = new WorldGenConfig.BiomeConfig();
                cfg.setBiomeSize(biomeSize);
                cfg.setTemperatureOffset(tempOffset);
                cfg.setHumidityOffset(humOffset);
                cfg.setBiomeBlending(blending);
                return new CustomBiomeSource(cfg, List.of());
            })
    );

    private final WorldGenConfig.BiomeConfig biomeConfig;
    private final List<RegistryEntry<Biome>> availableBiomes;
    private final long biomeSeed;

    public CustomBiomeSource(WorldGenConfig.BiomeConfig biomeConfig,
                             List<RegistryEntry<Biome>> allBiomes) {
        this.biomeConfig = biomeConfig;
        this.availableBiomes = filterBiomes(allBiomes, biomeConfig);
        this.biomeSeed = 0L;
    }

    public CustomBiomeSource(WorldGenConfig.BiomeConfig biomeConfig,
                             List<RegistryEntry<Biome>> allBiomes,
                             long seed) {
        this.biomeConfig = biomeConfig;
        this.availableBiomes = filterBiomes(allBiomes, biomeConfig);
        this.biomeSeed = seed;
    }

    /**
     * Creates a CustomBiomeSource from a biome registry, extracting all available biomes.
     */
    public static CustomBiomeSource fromRegistry(WorldGenConfig.BiomeConfig biomeConfig,
                                                  Registry<Biome> biomeRegistry,
                                                  long seed) {
        List<RegistryEntry<Biome>> biomes = biomeRegistry.streamEntries()
                .map(entry -> (RegistryEntry<Biome>) entry)
                .toList();
        return new CustomBiomeSource(biomeConfig, biomes, seed);
    }

    @Override
    protected MapCodec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    protected Stream<RegistryEntry<Biome>> biomeStream() {
        return availableBiomes.stream();
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        if (availableBiomes.isEmpty()) {
            throw new IllegalStateException("No biomes available in CustomBiomeSource");
        }

        // Force specific biomes if configured
        List<String> forced = biomeConfig.getForcedBiomes();
        if (forced != null && !forced.isEmpty()) {
            // Select among forced biomes using spatial hashing
            int idx = Math.floorMod(spatialHash(x, z), forced.size());
            String forcedName = forced.get(idx);
            for (RegistryEntry<Biome> entry : availableBiomes) {
                if (biomeIdMatches(entry, forcedName)) {
                    return entry;
                }
            }
        }

        int biomeSize = Math.max(1, biomeConfig.getBiomeSize());
        float blending = biomeConfig.getBiomeBlending();

        // Scale coordinates by biome size
        double scaledX = x / (double) biomeSize;
        double scaledZ = z / (double) biomeSize;

        // Noise-based temperature and humidity with offsets
        double temperature = NoiseGenerator.octaveNoise(NoiseType.SIMPLEX,
                scaledX * 0.01, 0, scaledZ * 0.01,
                biomeSeed, 3, 0.5)
                + biomeConfig.getTemperatureOffset();

        double humidity = NoiseGenerator.octaveNoise(NoiseType.SIMPLEX,
                scaledX * 0.01, 0, scaledZ * 0.01,
                biomeSeed + 5555, 3, 0.5)
                + biomeConfig.getHumidityOffset();

        // Apply blending smoothing
        if (blending > 1.0f) {
            temperature = smoothValue(temperature, blending);
            humidity = smoothValue(humidity, blending);
        }

        // Map the noise values to a biome index
        double combined = (temperature + 1.0) * 0.5 * availableBiomes.size()
                + (humidity + 1.0) * 0.25 * availableBiomes.size();
        int index = Math.floorMod((int) Math.round(combined), availableBiomes.size());

        return availableBiomes.get(index);
    }

    // ---- Helpers ----

    private static List<RegistryEntry<Biome>> filterBiomes(
            List<RegistryEntry<Biome>> allBiomes,
            WorldGenConfig.BiomeConfig config) {
        if (allBiomes == null || allBiomes.isEmpty()) {
            return allBiomes != null ? allBiomes : List.of();
        }

        List<String> disabled = config.getDisabledBiomes();
        if (disabled == null || disabled.isEmpty()) {
            return new ArrayList<>(allBiomes);
        }

        List<RegistryEntry<Biome>> filtered = new ArrayList<>();
        for (RegistryEntry<Biome> entry : allBiomes) {
            boolean isDisabled = false;
            for (String d : disabled) {
                if (biomeIdMatches(entry, d)) {
                    isDisabled = true;
                    break;
                }
            }
            if (!isDisabled) {
                filtered.add(entry);
            }
        }
        return filtered.isEmpty() ? new ArrayList<>(allBiomes) : filtered;
    }

    private static boolean biomeIdMatches(RegistryEntry<Biome> entry, String name) {
        return entry.getKey()
                .map(key -> key.getValue().toString().contains(name) ||
                        key.getValue().getPath().equalsIgnoreCase(name))
                .orElse(false);
    }

    private static int spatialHash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    private static double smoothValue(double value, float blendFactor) {
        double factor = 1.0 / blendFactor;
        return Math.tanh(value * factor) / Math.tanh(factor);
    }
}
