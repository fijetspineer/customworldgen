package com.customworldgen.gui;

import com.customworldgen.CustomWorldGenMod;
import com.customworldgen.config.PresetManager;
import com.customworldgen.config.WorldGenConfig;
import com.customworldgen.config.WorldGenPreset;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PresetSelectionScreen extends Screen {

    private final Screen parent;
    private final WorldGenConfig editingConfig;
    private final PresetManager presetManager;

    private List<WorldGenPreset> presets = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    private TextFieldWidget nameField;
    private ButtonWidget loadButton;
    private ButtonWidget deleteButton;
    private ButtonWidget exportButton;

    private static final int LIST_X = 20;
    private static final int LIST_Y = 40;
    private static final int LIST_ENTRY_HEIGHT = 18;
    private static final int LIST_VISIBLE_ENTRIES = 10;

    public PresetSelectionScreen(Screen parent, WorldGenConfig editingConfig) {
        super(Text.literal("Preset Selection"));
        this.parent = parent;
        this.editingConfig = editingConfig;
        this.presetManager = CustomWorldGenMod.getPresetManager();
    }

    @Override
    protected void init() {
        presets = presetManager.listPresets();

        int listWidth = this.width / 2 - 30;
        int rightPanelX = this.width / 2 + 10;
        int buttonWidth = this.width / 2 - 30;

        // Name text field for saving
        nameField = new TextFieldWidget(this.textRenderer, rightPanelX, LIST_Y, buttonWidth, 20,
                Text.literal("Preset Name"));
        nameField.setMaxLength(64);
        nameField.setPlaceholder(Text.literal("Enter preset name..."));
        addDrawableChild(nameField);

        int btnY = LIST_Y + 28;
        int btnSpacing = 24;

        // Save current config as preset
        addDrawableChild(ButtonWidget.builder(Text.literal("Save Current"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                WorldGenPreset preset = new WorldGenPreset(name, "User preset", editingConfig.copy());
                presetManager.savePreset(preset);
                presets = presetManager.listPresets();
                nameField.setText("");
            }
        }).dimensions(rightPanelX, btnY, buttonWidth, 20).build());

        // Load selected preset
        loadButton = ButtonWidget.builder(Text.literal("Load Selected"), button -> {
            if (selectedIndex >= 0 && selectedIndex < presets.size()) {
                WorldGenPreset preset = presets.get(selectedIndex);
                WorldGenConfig loaded = preset.getConfig();
                editingConfig.setTerrain(loaded.getTerrain().copy());
                editingConfig.setBiome(loaded.getBiome().copy());
                editingConfig.setStructure(loaded.getStructure().copy());
                editingConfig.setResource(loaded.getResource().copy());
                editingConfig.setEnvironment(loaded.getEnvironment().copy());
                editingConfig.setAdvanced(loaded.getAdvanced().copy());
            }
        }).dimensions(rightPanelX, btnY + btnSpacing, buttonWidth, 20).build();
        addDrawableChild(loadButton);

        // Delete selected preset
        deleteButton = ButtonWidget.builder(Text.literal("Delete Selected"), button -> {
            if (selectedIndex >= 0 && selectedIndex < presets.size()) {
                presetManager.deletePreset(presets.get(selectedIndex).getName());
                presets = presetManager.listPresets();
                selectedIndex = -1;
            }
        }).dimensions(rightPanelX, btnY + btnSpacing * 2, buttonWidth, 20).build();
        addDrawableChild(deleteButton);

        // Export (logs the JSON)
        exportButton = ButtonWidget.builder(Text.literal("Export to Log"), button -> {
            if (selectedIndex >= 0 && selectedIndex < presets.size()) {
                String json = presetManager.exportPresetJson(presets.get(selectedIndex));
                CustomWorldGenMod.LOGGER.info("Exported preset JSON:\n{}", json);
            }
        }).dimensions(rightPanelX, btnY + btnSpacing * 3, buttonWidth, 20).build();
        addDrawableChild(exportButton);

        // Import from name field (as JSON)
        addDrawableChild(ButtonWidget.builder(Text.literal("Import from Log"), button -> {
            String json = nameField.getText().trim();
            if (!json.isEmpty()) {
                try {
                    WorldGenPreset imported = presetManager.importPresetJson(json);
                    if (imported != null) {
                        presetManager.savePreset(imported);
                        presets = presetManager.listPresets();
                    }
                } catch (Exception e) {
                    CustomWorldGenMod.LOGGER.error("Failed to import preset JSON", e);
                }
            }
        }).dimensions(rightPanelX, btnY + btnSpacing * 4, buttonWidth, 20).build());

        // Done button
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button ->
                client.setScreen(parent))
                .dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedIndex >= 0 && selectedIndex < presets.size();
        loadButton.active = hasSelection;
        deleteButton.active = hasSelection;
        exportButton.active = hasSelection;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if click is within the preset list area
        int listWidth = this.width / 2 - 30;
        if (mouseX >= LIST_X && mouseX <= LIST_X + listWidth
                && mouseY >= LIST_Y && mouseY < LIST_Y + LIST_VISIBLE_ENTRIES * LIST_ENTRY_HEIGHT) {
            int clickedIndex = (int) ((mouseY - LIST_Y) / LIST_ENTRY_HEIGHT) + scrollOffset;
            if (clickedIndex >= 0 && clickedIndex < presets.size()) {
                selectedIndex = clickedIndex;
                updateButtonStates();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, presets.size() - LIST_VISIBLE_ENTRIES);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        int listWidth = this.width / 2 - 30;

        // Draw preset list
        for (int i = 0; i < LIST_VISIBLE_ENTRIES && (i + scrollOffset) < presets.size(); i++) {
            int index = i + scrollOffset;
            WorldGenPreset preset = presets.get(index);
            int entryY = LIST_Y + i * LIST_ENTRY_HEIGHT;

            // Highlight selected entry
            if (index == selectedIndex) {
                context.fill(LIST_X, entryY, LIST_X + listWidth, entryY + LIST_ENTRY_HEIGHT, 0x60FFFFFF);
            }

            // Draw preset name
            context.drawTextWithShadow(this.textRenderer,
                    Text.literal(preset.getName()), LIST_X + 4, entryY + 4, 0xFFFFFF);

            // Draw description in gray on the right
            String desc = preset.getDescription();
            if (desc != null && !desc.isEmpty()) {
                int maxDescWidth = listWidth - textRenderer.getWidth(preset.getName()) - 16;
                if (maxDescWidth > 20) {
                    String trimmed = textRenderer.trimToWidth(desc, maxDescWidth);
                    context.drawTextWithShadow(this.textRenderer,
                            Text.literal(" - " + trimmed),
                            LIST_X + 8 + textRenderer.getWidth(preset.getName()),
                            entryY + 4, 0xAAAAAA);
                }
            }
        }

        // Draw list border
        context.drawBorder(LIST_X - 1, LIST_Y - 1,
                listWidth + 2, LIST_VISIBLE_ENTRIES * LIST_ENTRY_HEIGHT + 2, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
