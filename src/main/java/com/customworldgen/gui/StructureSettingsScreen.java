package com.customworldgen.gui;

import com.customworldgen.config.WorldGenConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class StructureSettingsScreen extends Screen {

    private final Screen parent;
    private final WorldGenConfig config;

    public StructureSettingsScreen(Screen parent, WorldGenConfig config) {
        super(Text.literal("Structure Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int sliderWidth = 200;
        int x = centerX - sliderWidth / 2;
        int y = 32;
        int spacing = 24;

        WorldGenConfig.StructureConfig sc = config.getStructure();

        addDrawableChild(new CustomSliderWidget(x, y, sliderWidth, 20,
                "Village Rate", 0.0, 5.0, sc.getVillageRate(),
                v -> sc.setVillageRate(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing, sliderWidth, 20,
                "Stronghold Rate", 0.0, 5.0, sc.getStrongholdRate(),
                v -> sc.setStrongholdRate(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 2, sliderWidth, 20,
                "Mineshaft Rate", 0.0, 5.0, sc.getMineshaftRate(),
                v -> sc.setMineshaftRate(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 3, sliderWidth, 20,
                "Temple Rate", 0.0, 5.0, sc.getTempleRate(),
                v -> sc.setTempleRate(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 4, sliderWidth, 20,
                "Ancient City Rate", 0.0, 5.0, sc.getAncientCityRate(),
                v -> sc.setAncientCityRate(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 5, sliderWidth, 20,
                "Structure Density", 0.0, 3.0, sc.getStructureDensity(),
                v -> sc.setStructureDensity(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 6, sliderWidth, 20,
                "Min Structure Distance", 1, 32, sc.getMinStructureDistance(),
                v -> sc.setMinStructureDistance((int) Math.round(v))));

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
            if (max - min >= 10 && max <= 100) {
                return String.valueOf((int) Math.round(value));
            }
            return String.format("%.2f", value);
        }
    }
}
