package com.customworldgen.gui;

import com.customworldgen.config.NoiseType;
import com.customworldgen.config.WorldGenConfig;
import com.customworldgen.noise.NoiseGenerator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class NoiseVisualizerScreen extends Screen {

    private final Screen parent;
    private final WorldGenConfig config;

    private double offsetX = 0.0;
    private double offsetZ = 0.0;
    private double scale = 0.02;
    private NoiseType noiseType;

    private static final int PREVIEW_SIZE = 192;

    public NoiseVisualizerScreen(Screen parent, WorldGenConfig config) {
        super(Text.literal("Noise Visualizer"));
        this.parent = parent;
        this.config = config;
        this.noiseType = config.getTerrain().getNoiseType();
    }

    @Override
    protected void init() {
        int rightX = this.width / 2 + PREVIEW_SIZE / 2 + 16;
        int controlWidth = Math.min(160, this.width - rightX - 10);
        if (controlWidth < 80) {
            // If screen is too narrow, place controls below
            rightX = this.width / 2 - 80;
            controlWidth = 160;
        }
        int y = 40;
        int spacing = 24;

        addDrawableChild(new NoiseSliderWidget(rightX, y, controlWidth, 20,
                "X Offset", -500, 500, offsetX,
                v -> offsetX = v));

        addDrawableChild(new NoiseSliderWidget(rightX, y + spacing, controlWidth, 20,
                "Z Offset", -500, 500, offsetZ,
                v -> offsetZ = v));

        addDrawableChild(new NoiseSliderWidget(rightX, y + spacing * 2, controlWidth, 20,
                "Scale", 0.001, 0.1, scale,
                v -> scale = v));

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Noise: " + noiseType.getDisplayName()),
                button -> {
                    NoiseType[] types = NoiseType.values();
                    int next = (noiseType.ordinal() + 1) % types.length;
                    noiseType = types[next];
                    button.setMessage(Text.literal("Noise: " + noiseType.getDisplayName()));
                }).dimensions(rightX, y + spacing * 3, controlWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button ->
                client.setScreen(parent))
                .dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        // Draw noise preview
        int previewX = this.width / 2 - PREVIEW_SIZE / 2 - 60;
        int previewY = 40;

        // Render at lower resolution (4x4 pixel blocks) for performance
        int blockSize = 4;
        int steps = PREVIEW_SIZE / blockSize;

        for (int px = 0; px < steps; px++) {
            for (int pz = 0; pz < steps; pz++) {
                double nx = (px * blockSize + offsetX) * scale;
                double nz = (pz * blockSize + offsetZ) * scale;

                double noiseVal = NoiseGenerator.generateNoise(noiseType, nx, 0, nz, 12345L);
                // Normalize from [-1,1] to [0,1]
                noiseVal = (noiseVal + 1.0) * 0.5;
                noiseVal = Math.max(0.0, Math.min(1.0, noiseVal));

                int brightness = (int) (noiseVal * 255);
                int color = 0xFF000000 | (brightness << 16) | (brightness << 8) | brightness;

                context.fill(previewX + px * blockSize, previewY + pz * blockSize,
                        previewX + px * blockSize + blockSize, previewY + pz * blockSize + blockSize,
                        color);
            }
        }

        // Draw border around preview
        context.drawBorder(previewX - 1, previewY - 1, PREVIEW_SIZE + 2, PREVIEW_SIZE + 2, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private static class NoiseSliderWidget extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final Consumer<Double> callback;

        public NoiseSliderWidget(int x, int y, int width, int height,
                                 String label, double min, double max, double current,
                                 Consumer<Double> callback) {
            super(x, y, width, height, Text.literal(label + ": " + String.format("%.3f", current)),
                    (max != min) ? (current - min) / (max - min) : 0.0);
            this.label = label;
            this.min = min;
            this.max = max;
            this.callback = callback;
        }

        @Override
        protected void updateMessage() {
            double actual = min + value * (max - min);
            setMessage(Text.literal(label + ": " + String.format("%.3f", actual)));
        }

        @Override
        protected void applyValue() {
            double actual = min + value * (max - min);
            callback.accept(actual);
        }
    }
}
