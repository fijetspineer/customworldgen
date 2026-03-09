package com.customworldgen.mixin;

import com.customworldgen.gui.WorldGenSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void customworldgen$addCustomizeButton(CallbackInfo ci) {
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Customize World Gen"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new WorldGenSettingsScreen((Screen) (Object) this));
                    }
                })
                .dimensions(this.width / 2 - 75, this.height - 52, 150, 20)
                .build());
    }
}
