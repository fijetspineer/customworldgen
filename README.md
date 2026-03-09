# Custom World Generation Mod

A Minecraft Fabric mod for **Minecraft 1.21.1** that enables players to generate fully customizable worlds with advanced procedural generation controls.

## Features

### 🏔️ Terrain Generation Controls
- **Base terrain height** and **terrain scale/roughness** configuration
- Multiple **noise types**: Perlin, Simplex, Ridged Multifractal, Voronoi
- **Mountain frequency and height** controls
- **Valley depth** adjustment
- **Floating islands** generation with density control
- **Cave density** and **cave types** (Normal, Spaghetti, Cheese, Noodle)
- **Underground biomes** toggle

### 🌍 Biome Customization
- Biome **size and distribution** controls
- **Temperature** and **humidity** offset sliders
- **Biome rarity** adjustment
- Ability to **disable or force** specific biomes
- **Biome blending** control for smoother transitions

### 🏛️ Structure Generation
- Configurable spawn rates for: Villages, Strongholds, Mineshafts, Temples, Ancient Cities
- **Structure density** slider
- **Minimum distance** between structures
- Per-biome structure configuration

### 💎 Resource Distribution
- **Ore spawn height** range configuration
- **Ore frequency** multiplier
- **Cluster size** control
- Scarcity presets: Abundant, Normal, Scarce, Barren

### 🌤️ Environmental Rules
- Custom **day/night cycle length**
- **Weather frequency** control
- **River** and **ocean size** generation
- **Lava oceans** toggle
- **Gravity multiplier** (gameplay tweak)

### ⚡ Advanced Features
- **Skylands** world mode
- **Amplified** terrain with custom parameters
- **Multi-layer** terrain worlds
- **Biome blend radius** control
- **Noise visualizer** for debugging world generation

### 🎨 User Interface
- Full GUI accessible during world creation
- Sliders, toggles, and preset menus
- Categorized settings screens (Terrain, Biomes, Structures, Resources, Environment, Advanced)
- Noise visualization screen

### 💾 Preset System
- Create, save, edit, and load custom world presets
- Export/import presets as JSON files
- Built-in presets: Default, Amplified, Skylands, Cave World
- Presets selectable during world creation

### 🔧 Command System
- `/cwg reload` — Reload configuration
- `/cwg info` — Display current configuration summary
- `/cwg preset list` — List available presets
- `/cwg preset load <name>` — Load a preset
- `/cwg preset save <name>` — Save current config as a preset
- `/cwg terrain baseheight <value>` — Set base terrain height
- `/cwg terrain scale <value>` — Set terrain scale
- `/cwg biome size <value>` — Set biome size

## Requirements

- **Minecraft**: 1.21.1
- **Fabric Loader**: 0.16.5+
- **Fabric API**: 0.102.0+1.21.1
- **Java**: 21

## Building

### Prerequisites
- JDK 21 or later
- Internet connection (for downloading dependencies)

### Build Steps

```bash
# Clone the repository
git clone https://github.com/fijetspineer/customworldgen.git
cd customworldgen

# Build the mod
./gradlew build

# The compiled JAR will be in build/libs/
```

The output JAR file (`customworldgen-1.0.0.jar`) can be found in the `build/libs/` directory.

### Running in Development

```bash
# Run the Minecraft client with the mod loaded
./gradlew runClient

# Run a Minecraft server with the mod loaded
./gradlew runServer
```

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 1.21.1
3. Place `customworldgen-1.0.0.jar` into the `mods/` folder
4. Launch Minecraft

## Usage

### Creating a Custom World
1. Click **Singleplayer** → **Create New World**
2. Click the **"Customize World Gen"** button on the world creation screen
3. Configure settings across the category tabs:
   - **Terrain**: Height, scale, noise type, mountains, valleys, caves
   - **Biomes**: Size, temperature, humidity, rarity
   - **Structures**: Village/temple/mineshaft rates, density, distances
   - **Resources**: Ore frequency, heights, cluster sizes
   - **Environment**: Day length, weather, rivers, oceans
   - **Advanced**: Skylands, amplified mode, multi-layer terrain
4. Use the **Presets** tab to save/load configurations
5. Click **Apply** and create your world

### Working with Presets
Presets are stored as JSON files in `.minecraft/config/customworldgen/presets/`.

#### Example Preset JSON
See `src/main/resources/data/customworldgen/presets/example_custom.json` for a complete example.

#### Preset Structure
```json
{
  "name": "my_preset",
  "description": "A description of my custom world",
  "config": {
    "terrain": { ... },
    "biome": { ... },
    "structure": { ... },
    "resource": { ... },
    "environment": { ... },
    "advanced": { ... }
  }
}
```

## Project Structure

```
src/main/java/com/customworldgen/
├── CustomWorldGenMod.java          # Main mod entry point (server-side)
├── CustomWorldGenClient.java       # Client-side entry point
├── config/
│   ├── WorldGenConfig.java         # Master configuration with inner classes
│   ├── WorldGenPreset.java         # Preset data wrapper
│   ├── PresetManager.java          # Preset CRUD operations and JSON I/O
│   ├── NoiseType.java              # Noise algorithm enum
│   └── CaveType.java               # Cave generation type enum
├── noise/
│   └── NoiseGenerator.java         # Noise algorithms (Perlin, Simplex, Ridged, Voronoi)
├── worldgen/
│   ├── terrain/
│   │   ├── CustomChunkGenerator.java    # Custom chunk generation with noise-based terrain
│   │   └── CustomDensityFunction.java   # Custom density function for terrain shaping
│   ├── biome/
│   │   └── CustomBiomeSource.java       # Biome placement with config-driven parameters
│   ├── structure/
│   │   └── CustomStructurePlacement.java # Structure spawn rate and distance control
│   └── resource/
│       └── CustomOreGenerator.java      # Ore generation frequency and height control
├── environment/
│   └── EnvironmentManager.java     # Day/night cycle, weather, gravity management
├── gui/
│   ├── WorldGenSettingsScreen.java  # Main settings screen with category navigation
│   ├── TerrainSettingsScreen.java   # Terrain parameter sliders and toggles
│   ├── BiomeSettingsScreen.java     # Biome parameter sliders
│   ├── StructureSettingsScreen.java # Structure rate sliders
│   ├── ResourceSettingsScreen.java  # Resource distribution sliders
│   ├── EnvironmentSettingsScreen.java # Environment rule sliders
│   ├── AdvancedSettingsScreen.java  # Advanced feature toggles and sliders
│   ├── PresetSelectionScreen.java   # Preset management interface
│   └── NoiseVisualizerScreen.java   # Real-time noise visualization
├── command/
│   └── WorldGenCommands.java       # In-game commands (/cwg)
└── mixin/
    └── CreateWorldScreenMixin.java  # Injects customize button into world creation

src/main/resources/
├── fabric.mod.json                  # Fabric mod metadata
├── customworldgen.mixins.json       # Mixin configuration
├── customworldgen.accesswidener     # Access widener declarations
├── assets/customworldgen/
│   └── lang/en_us.json              # English translations
└── data/customworldgen/
    └── presets/example_custom.json  # Example world generation preset
```

## World Generation Systems

### Noise Generation
The mod uses four noise algorithms, each producing different terrain characteristics:
- **Perlin**: Smooth, natural-looking terrain with gentle gradients
- **Simplex**: Similar to Perlin but with fewer directional artifacts, good for organic shapes
- **Ridged Multifractal**: Creates sharp ridges and mountain ranges
- **Voronoi**: Cell-based noise producing distinct regions and plateaus

Noise is layered using **octave synthesis** where multiple noise passes at different frequencies are combined for detail at all scales.

### Terrain Generation
The `CustomChunkGenerator` uses the configured noise type to generate a height map, then fills blocks accordingly:
1. **Base terrain**: Defined by `baseHeight` and shaped by the selected noise algorithm
2. **Mountains**: Added on top using separate noise passes controlled by `mountainFrequency` and `mountainHeight`
3. **Valleys**: Carved below the base using `valleyDepth`
4. **Floating Islands**: Optional secondary terrain layer generated above the main surface
5. **Caves**: Carved using different algorithms (Spaghetti, Cheese, Noodle) based on `caveType`

### Biome Distribution
The `CustomBiomeSource` uses temperature and humidity noise maps to place biomes:
- `biomeSize` controls the scale of biome regions
- `temperatureOffset` and `humidityOffset` shift the climate globally
- Biomes can be disabled or forced through configuration
- Blending creates smooth transitions between adjacent biomes

### Structure Placement
Structure generation rates are controlled as multipliers on vanilla spawn rates. The `minStructureDistance` prevents structures from spawning too close together.

### Resource Distribution
Ore generation is modified by frequency multipliers and height range constraints. The scarcity preset system provides quick profiles (Abundant, Normal, Scarce, Barren).

## License

MIT License — see [LICENSE](LICENSE) for details.
