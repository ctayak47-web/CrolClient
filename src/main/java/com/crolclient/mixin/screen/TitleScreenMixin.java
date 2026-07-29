package com.crolclient.mixin.screen;

import com.crolclient.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    private static final Identifier[] BG = new Identifier[]{
        Identifier.of("crolclient", "textures/background/1.png"),
        Identifier.of("crolclient", "textures/background/2.png"),
        Identifier.of("crolclient", "textures/background/3.png"),
        Identifier.of("crolclient", "textures/background/4.png"),
        Identifier.of("crolclient", "textures/background/5.png"),
    };
    private static final Identifier STEVE = Identifier.of("crolclient", "textures/mainmenu/steve.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ConfigManager.getConfig().customBackgroundEnabled) {
            int idx = Integer.parseInt(ConfigManager.getConfig().customBackgroundMode) - 1;
            if (idx >= 0 && idx < BG.length) {
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    BG[idx],
                    0, 0,
                    0.0f, 0.0f,
                    context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                    context.getScaledWindowWidth(), context.getScaledWindowHeight()
                );
            }
        }
    }
}
