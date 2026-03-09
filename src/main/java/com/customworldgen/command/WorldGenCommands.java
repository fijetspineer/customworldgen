package com.customworldgen.command;

import com.customworldgen.CustomWorldGenMod;
import com.customworldgen.config.WorldGenConfig;
import com.customworldgen.config.WorldGenPreset;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public final class WorldGenCommands {

    private WorldGenCommands() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("customworldgen")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("reload").executes(ctx -> executeReload(ctx.getSource())))
                .then(CommandManager.literal("info").executes(ctx -> executeInfo(ctx.getSource())))
                .then(CommandManager.literal("preset")
                        .then(CommandManager.literal("list").executes(ctx -> executePresetList(ctx.getSource())))
                        .then(CommandManager.literal("load")
                                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> executePresetLoad(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name")))))
                        .then(CommandManager.literal("save")
                                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> executePresetSave(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"))))))
                .then(CommandManager.literal("terrain")
                        .then(CommandManager.literal("baseheight")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 256))
                                        .executes(ctx -> executeTerrainBaseHeight(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value")))))
                        .then(CommandManager.literal("scale")
                                .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.1f, 5.0f))
                                        .executes(ctx -> executeTerrainScale(ctx.getSource(),
                                                FloatArgumentType.getFloat(ctx, "value"))))))
                .then(CommandManager.literal("biome")
                        .then(CommandManager.literal("size")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(1, 10))
                                        .executes(ctx -> executeBiomeSize(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value")))))));

        // Register /cwg as an alias
        dispatcher.register(CommandManager.literal("cwg")
                .requires(source -> source.hasPermissionLevel(2))
                .redirect(dispatcher.getRoot().getChild("customworldgen")));
    }

    private static int executeReload(ServerCommandSource source) {
        CustomWorldGenMod.setCurrentConfig(WorldGenConfig.createDefault());
        source.sendFeedback(() -> Text.literal("§aWorld generation config reloaded to defaults."), true);
        return 1;
    }

    private static int executeInfo(ServerCommandSource source) {
        WorldGenConfig cfg = CustomWorldGenMod.getCurrentConfig();
        WorldGenConfig.TerrainConfig t = cfg.getTerrain();
        WorldGenConfig.BiomeConfig b = cfg.getBiome();
        WorldGenConfig.StructureConfig s = cfg.getStructure();

        String info = "§6--- Custom World Gen Config ---\n"
                + "§eTerrain: §fbase=" + t.getBaseHeight()
                + " scale=" + String.format("%.2f", t.getTerrainScale())
                + " noise=" + t.getNoiseType().getDisplayName() + "\n"
                + "§eMountains: §ffreq=" + String.format("%.2f", t.getMountainFrequency())
                + " height=" + String.format("%.2f", t.getMountainHeight()) + "\n"
                + "§eBiomes: §fsize=" + b.getBiomeSize()
                + " rarity=" + String.format("%.2f", b.getBiomeRarity()) + "\n"
                + "§eStructures: §fdensity=" + String.format("%.2f", s.getStructureDensity())
                + " minDist=" + s.getMinStructureDistance();

        source.sendFeedback(() -> Text.literal(info), false);
        return 1;
    }

    private static int executePresetList(ServerCommandSource source) {
        List<WorldGenPreset> presets = CustomWorldGenMod.getPresetManager().listPresets();
        if (presets.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§cNo presets found."), false);
            return 0;
        }
        StringBuilder sb = new StringBuilder("§6Available presets:\n");
        for (WorldGenPreset preset : presets) {
            sb.append("§e - §f").append(preset.getName());
            if (preset.getDescription() != null && !preset.getDescription().isEmpty()) {
                sb.append(" §7(").append(preset.getDescription()).append(")");
            }
            sb.append("\n");
        }
        source.sendFeedback(() -> Text.literal(sb.toString().trim()), false);
        return presets.size();
    }

    private static int executePresetLoad(ServerCommandSource source, String name) {
        WorldGenPreset preset = CustomWorldGenMod.getPresetManager().loadPreset(name);
        if (preset == null) {
            source.sendError(Text.literal("§cPreset '" + name + "' not found."));
            return 0;
        }
        CustomWorldGenMod.setCurrentConfig(preset.getConfig().copy());
        source.sendFeedback(() -> Text.literal("§aLoaded preset: " + name), true);
        return 1;
    }

    private static int executePresetSave(ServerCommandSource source, String name) {
        WorldGenPreset preset = new WorldGenPreset(name, "Saved via command",
                CustomWorldGenMod.getCurrentConfig().copy());
        CustomWorldGenMod.getPresetManager().savePreset(preset);
        source.sendFeedback(() -> Text.literal("§aSaved current config as preset: " + name), true);
        return 1;
    }

    private static int executeTerrainBaseHeight(ServerCommandSource source, int value) {
        CustomWorldGenMod.getCurrentConfig().getTerrain().setBaseHeight(value);
        source.sendFeedback(() -> Text.literal("§aBase height set to " + value), true);
        return 1;
    }

    private static int executeTerrainScale(ServerCommandSource source, float value) {
        CustomWorldGenMod.getCurrentConfig().getTerrain().setTerrainScale(value);
        source.sendFeedback(() -> Text.literal("§aTerrain scale set to " + String.format("%.2f", value)), true);
        return 1;
    }

    private static int executeBiomeSize(ServerCommandSource source, int value) {
        CustomWorldGenMod.getCurrentConfig().getBiome().setBiomeSize(value);
        source.sendFeedback(() -> Text.literal("§aBiome size set to " + value), true);
        return 1;
    }
}
