package com.customworldgen.config;

import java.util.ArrayList;
import java.util.List;

public class WorldGenConfig {

    private TerrainConfig terrain = new TerrainConfig();
    private BiomeConfig biome = new BiomeConfig();
    private StructureConfig structure = new StructureConfig();
    private ResourceConfig resource = new ResourceConfig();
    private EnvironmentConfig environment = new EnvironmentConfig();
    private AdvancedConfig advanced = new AdvancedConfig();

    public TerrainConfig getTerrain() {
        return terrain;
    }

    public void setTerrain(TerrainConfig terrain) {
        this.terrain = terrain;
    }

    public BiomeConfig getBiome() {
        return biome;
    }

    public void setBiome(BiomeConfig biome) {
        this.biome = biome;
    }

    public StructureConfig getStructure() {
        return structure;
    }

    public void setStructure(StructureConfig structure) {
        this.structure = structure;
    }

    public ResourceConfig getResource() {
        return resource;
    }

    public void setResource(ResourceConfig resource) {
        this.resource = resource;
    }

    public EnvironmentConfig getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentConfig environment) {
        this.environment = environment;
    }

    public AdvancedConfig getAdvanced() {
        return advanced;
    }

    public void setAdvanced(AdvancedConfig advanced) {
        this.advanced = advanced;
    }

    public static WorldGenConfig createDefault() {
        return new WorldGenConfig();
    }

    public WorldGenConfig copy() {
        WorldGenConfig copy = new WorldGenConfig();
        copy.terrain = this.terrain.copy();
        copy.biome = this.biome.copy();
        copy.structure = this.structure.copy();
        copy.resource = this.resource.copy();
        copy.environment = this.environment.copy();
        copy.advanced = this.advanced.copy();
        return copy;
    }

    // --- Inner config classes ---

    public static class TerrainConfig {
        public int baseHeight = 64;
        public float terrainScale = 1.0f;
        public NoiseType noiseType = NoiseType.PERLIN;
        public float mountainFrequency = 0.5f;
        public float mountainHeight = 1.0f;
        public float valleyDepth = 0.5f;
        public boolean floatingIslands = false;
        public float floatingIslandDensity = 0.3f;
        public float caveDensity = 0.5f;
        public CaveType caveType = CaveType.NORMAL;
        public boolean undergroundBiomes = true;

        public int getBaseHeight() { return baseHeight; }
        public void setBaseHeight(int baseHeight) { this.baseHeight = baseHeight; }
        public float getTerrainScale() { return terrainScale; }
        public void setTerrainScale(float terrainScale) { this.terrainScale = terrainScale; }
        public NoiseType getNoiseType() { return noiseType; }
        public void setNoiseType(NoiseType noiseType) { this.noiseType = noiseType; }
        public float getMountainFrequency() { return mountainFrequency; }
        public void setMountainFrequency(float mountainFrequency) { this.mountainFrequency = mountainFrequency; }
        public float getMountainHeight() { return mountainHeight; }
        public void setMountainHeight(float mountainHeight) { this.mountainHeight = mountainHeight; }
        public float getValleyDepth() { return valleyDepth; }
        public void setValleyDepth(float valleyDepth) { this.valleyDepth = valleyDepth; }
        public boolean isFloatingIslands() { return floatingIslands; }
        public void setFloatingIslands(boolean floatingIslands) { this.floatingIslands = floatingIslands; }
        public float getFloatingIslandDensity() { return floatingIslandDensity; }
        public void setFloatingIslandDensity(float floatingIslandDensity) { this.floatingIslandDensity = floatingIslandDensity; }
        public float getCaveDensity() { return caveDensity; }
        public void setCaveDensity(float caveDensity) { this.caveDensity = caveDensity; }
        public CaveType getCaveType() { return caveType; }
        public void setCaveType(CaveType caveType) { this.caveType = caveType; }
        public boolean isUndergroundBiomes() { return undergroundBiomes; }
        public void setUndergroundBiomes(boolean undergroundBiomes) { this.undergroundBiomes = undergroundBiomes; }

        public TerrainConfig copy() {
            TerrainConfig c = new TerrainConfig();
            c.baseHeight = this.baseHeight;
            c.terrainScale = this.terrainScale;
            c.noiseType = this.noiseType;
            c.mountainFrequency = this.mountainFrequency;
            c.mountainHeight = this.mountainHeight;
            c.valleyDepth = this.valleyDepth;
            c.floatingIslands = this.floatingIslands;
            c.floatingIslandDensity = this.floatingIslandDensity;
            c.caveDensity = this.caveDensity;
            c.caveType = this.caveType;
            c.undergroundBiomes = this.undergroundBiomes;
            return c;
        }
    }

    public static class BiomeConfig {
        public int biomeSize = 4;
        public float temperatureOffset = 0.0f;
        public float humidityOffset = 0.0f;
        public float biomeRarity = 1.0f;
        public List<String> disabledBiomes = new ArrayList<>();
        public List<String> forcedBiomes = new ArrayList<>();
        public float biomeBlending = 1.0f;

        public int getBiomeSize() { return biomeSize; }
        public void setBiomeSize(int biomeSize) { this.biomeSize = biomeSize; }
        public float getTemperatureOffset() { return temperatureOffset; }
        public void setTemperatureOffset(float temperatureOffset) { this.temperatureOffset = temperatureOffset; }
        public float getHumidityOffset() { return humidityOffset; }
        public void setHumidityOffset(float humidityOffset) { this.humidityOffset = humidityOffset; }
        public float getBiomeRarity() { return biomeRarity; }
        public void setBiomeRarity(float biomeRarity) { this.biomeRarity = biomeRarity; }
        public List<String> getDisabledBiomes() { return disabledBiomes; }
        public void setDisabledBiomes(List<String> disabledBiomes) { this.disabledBiomes = disabledBiomes; }
        public List<String> getForcedBiomes() { return forcedBiomes; }
        public void setForcedBiomes(List<String> forcedBiomes) { this.forcedBiomes = forcedBiomes; }
        public float getBiomeBlending() { return biomeBlending; }
        public void setBiomeBlending(float biomeBlending) { this.biomeBlending = biomeBlending; }

        public BiomeConfig copy() {
            BiomeConfig c = new BiomeConfig();
            c.biomeSize = this.biomeSize;
            c.temperatureOffset = this.temperatureOffset;
            c.humidityOffset = this.humidityOffset;
            c.biomeRarity = this.biomeRarity;
            c.disabledBiomes = new ArrayList<>(this.disabledBiomes);
            c.forcedBiomes = new ArrayList<>(this.forcedBiomes);
            c.biomeBlending = this.biomeBlending;
            return c;
        }
    }

    public static class StructureConfig {
        public float villageRate = 1.0f;
        public float strongholdRate = 1.0f;
        public float mineshaftRate = 1.0f;
        public float templeRate = 1.0f;
        public float ancientCityRate = 1.0f;
        public float structureDensity = 1.0f;
        public int minStructureDistance = 8;

        public float getVillageRate() { return villageRate; }
        public void setVillageRate(float villageRate) { this.villageRate = villageRate; }
        public float getStrongholdRate() { return strongholdRate; }
        public void setStrongholdRate(float strongholdRate) { this.strongholdRate = strongholdRate; }
        public float getMineshaftRate() { return mineshaftRate; }
        public void setMineshaftRate(float mineshaftRate) { this.mineshaftRate = mineshaftRate; }
        public float getTempleRate() { return templeRate; }
        public void setTempleRate(float templeRate) { this.templeRate = templeRate; }
        public float getAncientCityRate() { return ancientCityRate; }
        public void setAncientCityRate(float ancientCityRate) { this.ancientCityRate = ancientCityRate; }
        public float getStructureDensity() { return structureDensity; }
        public void setStructureDensity(float structureDensity) { this.structureDensity = structureDensity; }
        public int getMinStructureDistance() { return minStructureDistance; }
        public void setMinStructureDistance(int minStructureDistance) { this.minStructureDistance = minStructureDistance; }

        public StructureConfig copy() {
            StructureConfig c = new StructureConfig();
            c.villageRate = this.villageRate;
            c.strongholdRate = this.strongholdRate;
            c.mineshaftRate = this.mineshaftRate;
            c.templeRate = this.templeRate;
            c.ancientCityRate = this.ancientCityRate;
            c.structureDensity = this.structureDensity;
            c.minStructureDistance = this.minStructureDistance;
            return c;
        }
    }

    public static class ResourceConfig {
        public float oreFrequency = 1.0f;
        public int oreMinHeight = -64;
        public int oreMaxHeight = 320;
        public float clusterSize = 1.0f;
        public String scarcityPreset = "normal";

        public float getOreFrequency() { return oreFrequency; }
        public void setOreFrequency(float oreFrequency) { this.oreFrequency = oreFrequency; }
        public int getOreMinHeight() { return oreMinHeight; }
        public void setOreMinHeight(int oreMinHeight) { this.oreMinHeight = oreMinHeight; }
        public int getOreMaxHeight() { return oreMaxHeight; }
        public void setOreMaxHeight(int oreMaxHeight) { this.oreMaxHeight = oreMaxHeight; }
        public float getClusterSize() { return clusterSize; }
        public void setClusterSize(float clusterSize) { this.clusterSize = clusterSize; }
        public String getScarcityPreset() { return scarcityPreset; }
        public void setScarcityPreset(String scarcityPreset) { this.scarcityPreset = scarcityPreset; }

        public ResourceConfig copy() {
            ResourceConfig c = new ResourceConfig();
            c.oreFrequency = this.oreFrequency;
            c.oreMinHeight = this.oreMinHeight;
            c.oreMaxHeight = this.oreMaxHeight;
            c.clusterSize = this.clusterSize;
            c.scarcityPreset = this.scarcityPreset;
            return c;
        }
    }

    public static class EnvironmentConfig {
        public int dayLengthTicks = 24000;
        public float weatherFrequency = 1.0f;
        public float riverFrequency = 1.0f;
        public float oceanSize = 1.0f;
        public boolean lavaOceans = false;
        public float gravityMultiplier = 1.0f;

        public int getDayLengthTicks() { return dayLengthTicks; }
        public void setDayLengthTicks(int dayLengthTicks) { this.dayLengthTicks = dayLengthTicks; }
        public float getWeatherFrequency() { return weatherFrequency; }
        public void setWeatherFrequency(float weatherFrequency) { this.weatherFrequency = weatherFrequency; }
        public float getRiverFrequency() { return riverFrequency; }
        public void setRiverFrequency(float riverFrequency) { this.riverFrequency = riverFrequency; }
        public float getOceanSize() { return oceanSize; }
        public void setOceanSize(float oceanSize) { this.oceanSize = oceanSize; }
        public boolean isLavaOceans() { return lavaOceans; }
        public void setLavaOceans(boolean lavaOceans) { this.lavaOceans = lavaOceans; }
        public float getGravityMultiplier() { return gravityMultiplier; }
        public void setGravityMultiplier(float gravityMultiplier) { this.gravityMultiplier = gravityMultiplier; }

        public EnvironmentConfig copy() {
            EnvironmentConfig c = new EnvironmentConfig();
            c.dayLengthTicks = this.dayLengthTicks;
            c.weatherFrequency = this.weatherFrequency;
            c.riverFrequency = this.riverFrequency;
            c.oceanSize = this.oceanSize;
            c.lavaOceans = this.lavaOceans;
            c.gravityMultiplier = this.gravityMultiplier;
            return c;
        }
    }

    public static class AdvancedConfig {
        public boolean skylandsMode = false;
        public boolean amplifiedMode = false;
        public float amplifiedScale = 1.0f;
        public boolean multiLayerTerrain = false;
        public int terrainLayers = 1;
        public float biomeBlendRadius = 4.0f;

        public boolean isSkylandsMode() { return skylandsMode; }
        public void setSkylandsMode(boolean skylandsMode) { this.skylandsMode = skylandsMode; }
        public boolean isAmplifiedMode() { return amplifiedMode; }
        public void setAmplifiedMode(boolean amplifiedMode) { this.amplifiedMode = amplifiedMode; }
        public float getAmplifiedScale() { return amplifiedScale; }
        public void setAmplifiedScale(float amplifiedScale) { this.amplifiedScale = amplifiedScale; }
        public boolean isMultiLayerTerrain() { return multiLayerTerrain; }
        public void setMultiLayerTerrain(boolean multiLayerTerrain) { this.multiLayerTerrain = multiLayerTerrain; }
        public int getTerrainLayers() { return terrainLayers; }
        public void setTerrainLayers(int terrainLayers) { this.terrainLayers = terrainLayers; }
        public float getBiomeBlendRadius() { return biomeBlendRadius; }
        public void setBiomeBlendRadius(float biomeBlendRadius) { this.biomeBlendRadius = biomeBlendRadius; }

        public AdvancedConfig copy() {
            AdvancedConfig c = new AdvancedConfig();
            c.skylandsMode = this.skylandsMode;
            c.amplifiedMode = this.amplifiedMode;
            c.amplifiedScale = this.amplifiedScale;
            c.multiLayerTerrain = this.multiLayerTerrain;
            c.terrainLayers = this.terrainLayers;
            c.biomeBlendRadius = this.biomeBlendRadius;
            return c;
        }
    }
}
