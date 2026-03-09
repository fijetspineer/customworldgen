package com.customworldgen.gui;

import com.customworldgen.CustomWorldGenMod;
import com.customworldgen.config.WorldGenConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class WorldGenSettingsScreen extends Screen {

    private final Screen parent;
    private WorldGenConfig editingConfig;

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    public WorldGenSettingsScreen(Screen parent) {
        super(Text.literal("Custom World Generation Settings"));
        this.parent = parent;
        this.editingConfig = CustomWorldGenMod.getCurrentConfig().copy();
    }

    public WorldGenConfig getEditingConfig() {
        return editingConfig;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;

        // Category buttons in a two-column layout
        int leftCol = centerX - BUTTON_WIDTH - 5;
        int rightCol = centerX + 5;

        addDrawableChild(ButtonWidget.builder(Text.literal("Terrain"), button ->
                client.setScreen(new TerrainSettingsScreen(this, editingConfig)))
                .dimensions(leftCol, startY, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Biomes"), button ->
                client.setScreen(new BiomeSettingsScreen(this, editingConfig)))
                .dimensions(rightCol, startY, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Structures"), button ->
                client.setScreen(new StructureSettingsScreen(this, editingConfig)))
                .dimensions(leftCol, startY + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Resources"), button ->
                client.setScreen(new ResourceSettingsScreen(this, editingConfig)))
                .dimensions(rightCol, startY + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Environment"), button ->
                client.setScreen(new EnvironmentSettingsScreen(this, editingConfig)))
                .dimensions(leftCol, startY + BUTTON_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Advanced"), button ->
                client.setScreen(new AdvancedSettingsScreen(this, editingConfig)))
                .dimensions(rightCol, startY + BUTTON_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Presets"), button ->
                client.setScreen(new PresetSelectionScreen(this, editingConfig)))
                .dimensions(leftCol, startY + BUTTON_SPACING * 3, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Noise Visualizer"), button ->
                client.setScreen(new NoiseVisualizerScreen(this, editingConfig)))
                .dimensions(rightCol, startY + BUTTON_SPACING * 3, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        // Bottom action buttons
        int bottomY = this.height - 28;
        int actionWidth = 98;
        int totalWidth = actionWidth * 3 + 10;
        int actionStartX = centerX - totalWidth / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button -> {
            CustomWorldGenMod.setCurrentConfig(editingConfig);
            CustomWorldGenMod.LOGGER.info("World generation config applied");
            client.setScreen(parent);
        }).dimensions(actionStartX, bottomY, actionWidth, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            editingConfig = WorldGenConfig.createDefault();
            client.setScreen(new WorldGenSettingsScreen(parent));
        }).dimensions(actionStartX + actionWidth + 5, bottomY, actionWidth, BUTTON_HEIGHT).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button ->
                client.setScreen(parent))
                .dimensions(actionStartX + (actionWidth + 5) * 2, bottomY, actionWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
