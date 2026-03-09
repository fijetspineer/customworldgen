package com.customworldgen.worldgen.terrain;

import com.customworldgen.config.WorldGenConfig;
import com.customworldgen.noise.NoiseGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

/**
 * A custom density function that uses our {@link NoiseGenerator} and terrain
 * configuration to produce density values for world generation.
 */
public class CustomDensityFunction implements DensityFunction {

    public static final MapCodec<CustomDensityFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("base_height", 64).forGetter(f -> f.terrainConfig.getBaseHeight()),
                    Codec.FLOAT.optionalFieldOf("terrain_scale", 1.0f).forGetter(f -> f.terrainConfig.getTerrainScale()),
                    Codec.DOUBLE.optionalFieldOf("frequency", 0.005).forGetter(f -> f.frequency),
                    Codec.LONG.optionalFieldOf("seed", 0L).forGetter(f -> f.seed)
            ).apply(instance, (baseHeight, terrainScale, frequency, seed) -> {
                WorldGenConfig.TerrainConfig cfg = new WorldGenConfig.TerrainConfig();
                cfg.setBaseHeight(baseHeight);
                cfg.setTerrainScale(terrainScale);
                return new CustomDensityFunction(cfg, frequency, seed);
            })
    );

    public static final CodecHolder<CustomDensityFunction> CODEC_HOLDER =
            CodecHolder.of(MAP_CODEC);

    private final WorldGenConfig.TerrainConfig terrainConfig;
    private final double frequency;
    private final long seed;

    public CustomDensityFunction(WorldGenConfig.TerrainConfig terrainConfig, double frequency, long seed) {
        this.terrainConfig = terrainConfig;
        this.frequency = frequency;
        this.seed = seed;
    }

    /**
     * Samples the density at a given point. Negative values represent solid blocks,
     * positive values represent air.
     */
    @Override
    public double sample(DensityFunction.NoisePos pos) {
        int x = pos.blockX();
        int y = pos.blockY();
        int z = pos.blockZ();

        int baseHeight = terrainConfig.getBaseHeight();
        float terrainScale = terrainConfig.getTerrainScale();

        // Noise-based density: 3D noise combined with a vertical gradient
        double noiseValue = NoiseGenerator.octaveNoise(
                terrainConfig.getNoiseType(),
                x * frequency * terrainScale,
                y * frequency * terrainScale * 0.5,
                z * frequency * terrainScale,
                seed, 4, 0.5);

        // Vertical gradient: below baseHeight is solid, above is air
        double gradient = (baseHeight - y) / (double) baseHeight;

        // Mountain contribution
        double mountainNoise = NoiseGenerator.generateNoise(
                terrainConfig.getNoiseType(),
                x * frequency * 0.5 * terrainConfig.getMountainFrequency(),
                0,
                z * frequency * 0.5 * terrainConfig.getMountainFrequency(),
                seed + 1234);
        mountainNoise = Math.max(0, mountainNoise) * terrainConfig.getMountainHeight();

        // Combine: positive = solid, negative = air (convention for density functions)
        double density = gradient + noiseValue * 0.5 + mountainNoise * 0.3;

        return -density; // Minecraft convention: negative = solid
    }

    @Override
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        applier.fill(densities, this);
    }

    @Override
    public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return CODEC_HOLDER;
    }
}
