package com.customworldgen.gui;

import com.customworldgen.config.WorldGenConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class AdvancedSettingsScreen extends Screen {

    private final Screen parent;
    private final WorldGenConfig config;

    public AdvancedSettingsScreen(Screen parent, WorldGenConfig config) {
        super(Text.literal("Advanced Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int sliderWidth = 200;
        int x = centerX - sliderWidth / 2;
        int y = 40;
        int spacing = 26;

        WorldGenConfig.AdvancedConfig ac = config.getAdvanced();

        // Toggle buttons
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Skylands Mode: " + (ac.isSkylandsMode() ? "ON" : "OFF")),
                button -> {
                    ac.setSkylandsMode(!ac.isSkylandsMode());
                    button.setMessage(Text.literal("Skylands Mode: " + (ac.isSkylandsMode() ? "ON" : "OFF")));
                }).dimensions(x, y, sliderWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Amplified Mode: " + (ac.isAmplifiedMode() ? "ON" : "OFF")),
                button -> {
                    ac.setAmplifiedMode(!ac.isAmplifiedMode());
                    button.setMessage(Text.literal("Amplified Mode: " + (ac.isAmplifiedMode() ? "ON" : "OFF")));
                }).dimensions(x, y + spacing, sliderWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Multi-Layer Terrain: " + (ac.isMultiLayerTerrain() ? "ON" : "OFF")),
                button -> {
                    ac.setMultiLayerTerrain(!ac.isMultiLayerTerrain());
                    button.setMessage(Text.literal("Multi-Layer Terrain: " + (ac.isMultiLayerTerrain() ? "ON" : "OFF")));
                }).dimensions(x, y + spacing * 2, sliderWidth, 20).build());

        // Sliders
        addDrawableChild(new CustomSliderWidget(x, y + spacing * 3, sliderWidth, 20,
                "Amplified Scale", 0.5, 3.0, ac.getAmplifiedScale(),
                v -> ac.setAmplifiedScale(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 4, sliderWidth, 20,
                "Terrain Layers", 1, 5, ac.getTerrainLayers(),
                v -> ac.setTerrainLayers((int) Math.round(v))));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 5, sliderWidth, 20,
                "Biome Blend Radius", 0.0, 16.0, ac.getBiomeBlendRadius(),
                v -> ac.setBiomeBlendRadius(v.floatValue())));

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
            if (max - min >= 3 && max <= 20) {
                return String.valueOf((int) Math.round(value));
            }
            return String.format("%.2f", value);
        }
    }
}
