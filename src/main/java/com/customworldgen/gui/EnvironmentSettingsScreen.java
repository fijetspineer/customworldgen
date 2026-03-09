package com.customworldgen.gui;

import com.customworldgen.config.WorldGenConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class EnvironmentSettingsScreen extends Screen {

    private final Screen parent;
    private final WorldGenConfig config;

    public EnvironmentSettingsScreen(Screen parent, WorldGenConfig config) {
        super(Text.literal("Environment Settings"));
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

        WorldGenConfig.EnvironmentConfig ec = config.getEnvironment();

        addDrawableChild(new CustomSliderWidget(x, y, sliderWidth, 20,
                "Day Length (ticks)", 1000, 120000, ec.getDayLengthTicks(),
                v -> ec.setDayLengthTicks((int) Math.round(v))));

        addDrawableChild(new CustomSliderWidget(x, y + spacing, sliderWidth, 20,
                "Weather Frequency", 0.0, 3.0, ec.getWeatherFrequency(),
                v -> ec.setWeatherFrequency(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 2, sliderWidth, 20,
                "River Frequency", 0.0, 3.0, ec.getRiverFrequency(),
                v -> ec.setRiverFrequency(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 3, sliderWidth, 20,
                "Ocean Size", 0.1, 5.0, ec.getOceanSize(),
                v -> ec.setOceanSize(v.floatValue())));

        addDrawableChild(new CustomSliderWidget(x, y + spacing * 4, sliderWidth, 20,
                "Gravity Multiplier", 0.1, 3.0, ec.getGravityMultiplier(),
                v -> ec.setGravityMultiplier(v.floatValue())));

        // Lava oceans toggle
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Lava Oceans: " + (ec.isLavaOceans() ? "ON" : "OFF")),
                button -> {
                    ec.setLavaOceans(!ec.isLavaOceans());
                    button.setMessage(Text.literal("Lava Oceans: " + (ec.isLavaOceans() ? "ON" : "OFF")));
                }).dimensions(x, y + spacing * 5, sliderWidth, 20).build());

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
            if (max - min >= 10) {
                return String.valueOf((int) Math.round(value));
            }
            return String.format("%.2f", value);
        }
    }
}
