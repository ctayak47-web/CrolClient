package ru.crolclient.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.render.Render2DEvent;
import ru.crolclient.implement.features.modules.render.InterfaceModule;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        if (interfaceModule.isState() && interfaceModule.getInterfaceSettings().isSelected("HotBar")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusBars(DrawContext context, CallbackInfo ci) {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        if (interfaceModule.isState() && interfaceModule.getInterfaceSettings().isSelected("HotBar")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void onRenderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        InterfaceModule interfaceModule = (InterfaceModule) Extra.getInstance().getModuleProvider().module("Interface");
        if (interfaceModule.isState() && interfaceModule.getInterfaceSettings().isSelected("HotBar")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusEffects(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (client != null && client.getWindow() != null) {
            EventManager.callEvent(new Render2DEvent(
                    context.getMatrices(),
                    tickCounter.getTickDelta(false),
                    client.getWindow().getScaledWidth(),
                    client.getWindow().getScaledHeight()
            ));
        }
    }
}
