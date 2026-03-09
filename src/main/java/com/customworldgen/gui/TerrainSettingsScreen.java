package com.customworldgen.gui;

import com.customworldgen.config.CaveType;
import com.customworldgen.config.NoiseType;
import com.customworldgen.config.WorldGenConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class TerrainSettingsScreen extends Screen {

    private final Screen parent;
    private final WorldGenConfig config;

    public TerrainSettingsScreen(Screen parent, WorldGenConfig config) {
        super(Text.literal("Terrain Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int sliderWidth = 200;
        int x = centerX - sliderWidth / 2;
        int y = 32;
        int spacing = 22;

        WorldGenConfig.TerrainConfig tc = config.getTerrain();

        addDrawableChild(new CustomSliderWidget(x, y, sliderWidth, 20,
                "Base Height", 0, 256, tc.getBaseHeight(),
                v -> tc.setBaseHeight((int) Math.round(v))));

        addDrawableChild(new CustomSliderWidget(x, y + spacing, sliderWidth, 20,
                "Terrain Scale", 0.1, 5.0, tc.getTerrainScale(),
                v -> tc.setTerrainScale(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 2, sliderWidth, 20,
                "Mountain Frequency", 0.0, 2.0, tc.getMountainFrequency(),
                v -> tc.setMountainFrequency(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 3, sliderWidth, 20,
                "Mountain Height", 0.0, 3.0, tc.getMountainHeight(),
                v -> tc.setMountainHeight(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 4, sliderWidth, 20,
                "Valley Depth", 0.0, 2.0, tc.getValleyDepth(),
                v -> tc.setValleyDepth(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 5, sliderWidth, 20,
                "Cave Density", 0.0, 2.0, tc.getCaveDensity(),
                v -> tc.setCaveDensity(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 6, sliderWidth, 20,
                "Floating Island Density", 0.0, 1.0, tc.getFloatingIslandDensity(),
                v -> tc.setFloatingIslandDensity(v.floatValue())));

        // Toggle buttons
        int toggleY = y + spacing * 7;
        int halfWidth = sliderWidth / 2 - 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Floating Islands: " + (tc.isFloatingIslands() ? "ON" : "OFF")),
                button -> {
                    tc.setFloatingIslands(!tc.isFloatingIslands());
                    button.setMessage(Text.literal("Floating Islands: " + (tc.isFloatingIslands() ? "ON" : "OFF")));
                }).dimensions(x, toggleY, halfWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Underground Biomes: " + (tc.isUndergroundBiomes() ? "ON" : "OFF")),
                button -> {
                    tc.setUndergroundBiomes(!tc.isUndergroundBiomes());
                    button.setMessage(Text.literal("Underground Biomes: " + (tc.isUndergroundBiomes() ? "ON" : "OFF")));
                }).dimensions(x + halfWidth + 4, toggleY, halfWidth, 20).build());

        // Cycle buttons for enums
        int cycleY = toggleY + spacing;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Noise: " + tc.getNoiseType().getDisplayName()),
                button -> {
                    NoiseType[] types = NoiseType.values();
                    int next = (tc.getNoiseType().ordinal() + 1) % types.length;
                    tc.setNoiseType(types[next]);
                    button.setMessage(Text.literal("Noise: " + tc.getNoiseType().getDisplayName()));
                }).dimensions(x, cycleY, halfWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Cave: " + tc.getCaveType().getDisplayName()),
                button -> {
                    CaveType[] types = CaveType.values();
                    int next = (tc.getCaveType().ordinal() + 1) % types.length;
                    tc.setCaveType(types[next]);
                    button.setMessage(Text.literal("Cave: " + tc.getCaveType().getDisplayName()));
                }).dimensions(x + halfWidth + 4, cycleY, halfWidth, 20).build());

        // Done button
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button ->
                client.setScreen(parent))
                .dimensions(centerX - 100, this.height - 28, 200, 20).build());
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

    private static class CustomSliderWidget extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final Consumer<Double> callback;

        public CustomSliderWidget(int x, int y, int width, int height,
                                  String label, double min, double max, double current,
                                  Consumer<Double> callback) {
            super(x, y, width, height, Text.literal(label + ": " + formatValue(current, min, max)),
                    (max != min) ? (current - min) / (max - min) : 0.0);
            this.label = label;
            this.min = min;
            this.max = max;
            this.callback = callback;
        }

        @Override
        protected void updateMessage() {
            double actual = min + value * (max - min);
            setMessage(Text.literal(label + ": " + formatValue(actual, min, max)));
        }

        @Override
        protected void applyValue() {
            double actual = min + value * (max - min);
            callback.accept(actual);
        }

        private static String formatValue(double value, double min, double max) {
            if (max - min >= 10 && max <= 1000) {
                return String.valueOf((int) Math.round(value));
            }
            return String.format("%.2f", value);
        }
    }
}
