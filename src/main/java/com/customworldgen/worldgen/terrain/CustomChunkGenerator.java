package com.customworldgen.worldgen.terrain;

import com.customworldgen.CustomWorldGenMod;
import com.customworldgen.config.CaveType;
import com.customworldgen.config.NoiseType;
import com.customworldgen.config.WorldGenConfig;
import com.customworldgen.noise.NoiseGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A custom chunk generator that uses {@link WorldGenConfig} terrain settings
 * together with our {@link NoiseGenerator} to produce terrain.
 */
public class CustomChunkGenerator extends ChunkGenerator {

    /**
     * Codec for serialization/deserialization of this chunk generator.
     * Encodes the core terrain parameters needed to reproduce the world.
     */
    public static final MapCodec<CustomChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource),
                    Codec.INT.optionalFieldOf("base_height", 64).forGetter(gen -> gen.config.getTerrain().getBaseHeight()),
                    Codec.FLOAT.optionalFieldOf("terrain_scale", 1.0f).forGetter(gen -> gen.config.getTerrain().getTerrainScale()),
                    Codec.BOOL.optionalFieldOf("floating_islands", false).forGetter(gen -> gen.config.getTerrain().isFloatingIslands())
            ).apply(instance, (biomeSource, baseHeight, terrainScale, floatingIslands) -> {
                WorldGenConfig cfg = WorldGenConfig.createDefault();
                cfg.getTerrain().setBaseHeight(baseHeight);
                cfg.getTerrain().setTerrainScale(terrainScale);
                cfg.getTerrain().setFloatingIslands(floatingIslands);
                return new CustomChunkGenerator(biomeSource, cfg);
            })
    );

    private final WorldGenConfig config;
    private long worldSeed;

    public CustomChunkGenerator(BiomeSource biomeSource, WorldGenConfig config) {
        super(biomeSource);
        this.config = config;
        this.worldSeed = 0L;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    // ---- Dimension metrics ----

    @Override
    public int getWorldHeight() {
        return 384;
    }

    @Override
    public int getMinimumY() {
        return -64;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        int terrainHeight = computeSurfaceHeight(x, z, this.worldSeed);
        return Math.min(terrainHeight, world.getTopY() - 1);
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        int minY = world.getBottomY();
        int height = world.getHeight();
        BlockState[] column = new BlockState[height];

        int surfaceY = computeSurfaceHeight(x, z, this.worldSeed);
        BlockState stone = Blocks.STONE.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState water = Blocks.WATER.getDefaultState();
        BlockState lava = Blocks.LAVA.getDefaultState();
        BlockState fluid = config.getEnvironment().isLavaOceans() ? lava : water;

        for (int i = 0; i < height; i++) {
            int y = minY + i;
            if (y < surfaceY) {
                column[i] = stone;
            } else if (y < getSeaLevel()) {
                column[i] = fluid;
            } else {
                column[i] = air;
            }
        }

        return new VerticalBlockSample(minY, column);
    }

    // ---- Main generation ----

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig,
                                                   StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            long seed = this.worldSeed;

            WorldGenConfig.TerrainConfig terrain = config.getTerrain();
            NoiseType noiseType = terrain.getNoiseType();
            int baseHeight = terrain.getBaseHeight();
            float terrainScale = terrain.getTerrainScale();
            float mountainFreq = terrain.getMountainFrequency();
            float mountainHeight = terrain.getMountainHeight();
            float valleyDepth = terrain.getValleyDepth();
            boolean floatingIslands = terrain.isFloatingIslands();
            float islandDensity = terrain.getFloatingIslandDensity();

            int startX = chunk.getPos().getStartX();
            int startZ = chunk.getPos().getStartZ();
            int minY = chunk.getBottomY();
            int maxY = chunk.getTopY() - 1;

            BlockState stone = Blocks.STONE.getDefaultState();
            BlockState water = Blocks.WATER.getDefaultState();
            BlockState lava = Blocks.LAVA.getDefaultState();
            BlockState bedrock = Blocks.BEDROCK.getDefaultState();
            BlockState fluid = config.getEnvironment().isLavaOceans() ? lava : water;

            BlockPos.Mutable pos = new BlockPos.Mutable();

            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    int worldX = startX + localX;
                    int worldZ = startZ + localZ;

                    int surfaceY = computeSurfaceHeightFull(worldX, worldZ, seed,
                            noiseType, baseHeight, terrainScale, mountainFreq,
                            mountainHeight, valleyDepth);

                    for (int y = minY; y <= maxY; y++) {
                        pos.set(worldX, y, worldZ);

                        if (y == minY) {
                            chunk.setBlockState(pos, bedrock, false);
                            continue;
                        }

                        // Floating islands
                        if (floatingIslands && y > baseHeight + 40) {
                            double islandNoise = NoiseGenerator.octaveNoise(noiseType,
                                    worldX * 0.01, y * 0.02, worldZ * 0.01,
                                    seed + 9999, 4, 0.5);
                            double threshold = 0.6 - (islandDensity * 0.3);
                            if (islandNoise > threshold) {
                                chunk.setBlockState(pos, stone, false);
                                continue;
                            }
                        }

                        // Regular terrain
                        if (y < surfaceY) {
                            // Cave carving
                            if (shouldCarveCave(worldX, y, worldZ, seed, terrain)) {
                                if (y < getSeaLevel()) {
                                    chunk.setBlockState(pos, fluid, false);
                                }
                                // else leave as air (default)
                            } else {
                                chunk.setBlockState(pos, stone, false);
                            }
                        } else if (y < getSeaLevel()) {
                            chunk.setBlockState(pos, fluid, false);
                        }
                        // Above surface is air (default)
                    }
                }
            }

            return chunk;
        });
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures,
                             NoiseConfig noiseConfig, Chunk chunk) {
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        long seed = this.worldSeed;

        BlockState grass = Blocks.GRASS_BLOCK.getDefaultState();
        BlockState dirt = Blocks.DIRT.getDefaultState();
        BlockState sand = Blocks.SAND.getDefaultState();
        BlockState sandstone = Blocks.SANDSTONE.getDefaultState();
        BlockState gravel = Blocks.GRAVEL.getDefaultState();

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;
                int surfaceY = computeSurfaceHeight(worldX, worldZ, seed);

                if (surfaceY <= getSeaLevel() + 1) {
                    // Beach / ocean floor
                    for (int depth = 0; depth < 4 && surfaceY - depth >= chunk.getBottomY(); depth++) {
                        pos.set(worldX, surfaceY - depth, worldZ);
                        BlockState current = chunk.getBlockState(pos);
                        if (!current.isAir() && current.getBlock() != Blocks.WATER && current.getBlock() != Blocks.LAVA) {
                            chunk.setBlockState(pos, depth == 0 ? sand : (depth < 3 ? sand : sandstone), false);
                        }
                    }
                } else {
                    // Normal surface
                    for (int depth = 0; depth < 4 && surfaceY - 1 - depth >= chunk.getBottomY(); depth++) {
                        pos.set(worldX, surfaceY - 1 - depth, worldZ);
                        BlockState current = chunk.getBlockState(pos);
                        if (!current.isAir()) {
                            chunk.setBlockState(pos, depth == 0 ? grass : dirt, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig,
                      BiomeAccess biomeAccess, StructureAccessor structureAccessor,
                      Chunk chunk, GenerationStep.Carver carverStep) {
        // Cave carving is handled inline during populateNoise for efficiency
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        // Default entity population – delegated to vanilla mob spawning
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        text.add("CustomWorldGen ChunkGenerator");
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk,
                                 StructureAccessor structureAccessor) {
        // Feature generation (trees, ores, etc.) can be handled by vanilla or custom feature placement
    }

    // ---- Terrain height computation ----

    private int computeSurfaceHeight(int x, int z, long seed) {
        WorldGenConfig.TerrainConfig terrain = config.getTerrain();
        return computeSurfaceHeightFull(x, z, seed,
                terrain.getNoiseType(), terrain.getBaseHeight(),
                terrain.getTerrainScale(), terrain.getMountainFrequency(),
                terrain.getMountainHeight(), terrain.getValleyDepth());
    }

    private int computeSurfaceHeightFull(int x, int z, long seed,
                                         NoiseType noiseType, int baseHeight,
                                         float terrainScale, float mountainFreq,
                                         float mountainHeight, float valleyDepth) {
        // Base terrain noise
        double baseNoise = NoiseGenerator.octaveNoise(noiseType,
                x * 0.005 * terrainScale,
                0,
                z * 0.005 * terrainScale,
                seed, 6, 0.5);

        // Mountain noise (higher frequency, additive)
        double mountainNoise = NoiseGenerator.octaveNoise(noiseType,
                x * 0.002 * mountainFreq,
                0,
                z * 0.002 * mountainFreq,
                seed + 1000, 4, 0.45);
        mountainNoise = Math.max(0, mountainNoise);

        // Valley noise (creates depressions)
        double valleyNoise = NoiseGenerator.octaveNoise(NoiseType.PERLIN,
                x * 0.003,
                0,
                z * 0.003,
                seed + 2000, 3, 0.5);
        valleyNoise = Math.max(0, -valleyNoise);

        double height = baseHeight
                + baseNoise * 32.0 * terrainScale
                + mountainNoise * 64.0 * mountainHeight
                - valleyNoise * 32.0 * valleyDepth;

        // Amplified mode support
        if (config.getAdvanced().isAmplifiedMode()) {
            height = baseHeight + (height - baseHeight) * config.getAdvanced().getAmplifiedScale();
        }

        return (int) Math.round(Math.max(getMinimumY() + 1, Math.min(getMinimumY() + getWorldHeight() - 1, height)));
    }

    // ---- Cave carving ----

    private boolean shouldCarveCave(int x, int y, int z, long seed, WorldGenConfig.TerrainConfig terrain) {
        float caveDensity = terrain.getCaveDensity();
        if (caveDensity <= 0.0f) {
            return false;
        }

        CaveType caveType = terrain.getCaveType();
        double noise;

        switch (caveType) {
            case SPAGHETTI -> {
                double n1 = NoiseGenerator.perlinNoise(x * 0.04, y * 0.04, z * 0.04, seed + 3000);
                double n2 = NoiseGenerator.perlinNoise(x * 0.04, y * 0.04, z * 0.04, seed + 4000);
                noise = n1 * n1 + n2 * n2;
                return noise < 0.02 * caveDensity;
            }
            case CHEESE -> {
                noise = NoiseGenerator.octaveNoise(NoiseType.PERLIN,
                        x * 0.02, y * 0.03, z * 0.02,
                        seed + 5000, 3, 0.5);
                return noise > (0.7 - caveDensity * 0.3);
            }
            case NOODLE -> {
                double nx = NoiseGenerator.perlinNoise(x * 0.06, y * 0.06, z * 0.06, seed + 6000);
                double nz = NoiseGenerator.perlinNoise(x * 0.06, y * 0.06, z * 0.06, seed + 7000);
                noise = nx * nx + nz * nz;
                return noise < 0.01 * caveDensity;
            }
            default -> {
                // NORMAL caves
                noise = NoiseGenerator.octaveNoise(NoiseType.PERLIN,
                        x * 0.03, y * 0.05, z * 0.03,
                        seed + 8000, 3, 0.5);
                return noise > (0.75 - caveDensity * 0.25);
            }
        }
    }
}
